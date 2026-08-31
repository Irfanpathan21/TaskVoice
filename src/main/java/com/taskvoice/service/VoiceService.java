package com.taskvoice.service;

import com.taskvoice.ai.*;
import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * VoiceService — manages the full voice timesheet flow:
 * 1. Save transcript/audio as VoiceRecord (DRAFT)
 * 2. Send to Gemini for segmentation
 * 3. Validate response
 * 4. Return parsed blocks for employee review
 * 5. On confirmation, save to timesheet_entries
 *
 * Never loses input on AI failure — always saves the transcript as DRAFT for retry.
 */
public class VoiceService {

    private static final Logger log = LoggerFactory.getLogger(VoiceService.class);

    private final VoiceRecordDAO   voiceRecordDAO = new VoiceRecordDAOImpl();
    private final TimesheetDAO     timesheetDAO   = new TimesheetDAOImpl();
    private final TaskDAO          taskDAO        = new TaskDAOImpl();
    private final GeminiClient     gemini         = new GeminiClient();
    private final GroqWhisperClient whisperClient = new GroqWhisperClient();

    /**
     * Process audio bytes using Groq Whisper API (whisper-large-v3-turbo), with text fallback.
     */
    public ParseResult processAudioData(int userId, byte[] audioBytes, String mimeType, String fallbackTranscript) {
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        String finalTranscript = fallbackTranscript;

        if (audioBytes != null && audioBytes.length > 0) {
            try {
                String whisperText = whisperClient.transcribe(audioBytes, mimeType, correlationId);
                if (whisperText != null && !whisperText.isBlank()) {
                    finalTranscript = whisperText.trim();
                }
            } catch (Exception e) {
                log.warn("[{}] Groq Whisper transcription failed: {}. Falling back to browser transcript.", correlationId, e.getMessage());
            }
        }

        return processTranscript(userId, finalTranscript, null);
    }

    /**
     * Step 1+2+3: Save transcript, call Gemini, return parsed work blocks.
     * On AI failure, transcript is preserved as DRAFT — never lost.
     *
     * @param userId the employee
     * @param transcript raw speech-to-text transcript
     * @param audioFileRef optional path/ref to audio file (may be null)
     * @return ParseResult containing either parsed blocks or draft ID for retry
     */
    public ParseResult processTranscript(int userId, String transcript, String audioFileRef) {
        // Save transcript immediately — never lose it
        VoiceRecord record = new VoiceRecord();
        record.setUserId(userId);
        record.setTranscript(transcript);
        record.setAudioFileRef(audioFileRef);
        record.setProcessingStatus("PROCESSING");
        int recordId = voiceRecordDAO.insert(record);

        String correlationId = UUID.randomUUID().toString().substring(0, 8);

        try {
            // Get employee's current assigned tasks for fuzzy matching
            List<Task> assignedTasks = taskDAO.findByAssigneeId(userId, 1, 50);
            List<String> taskTitles = assignedTasks.stream().map(Task::getTitle).toList();

            // Build prompt and call Gemini
            String prompt = GeminiPrompts.voiceSegmentation(transcript, taskTitles);
            String rawResponse = gemini.call(prompt, correlationId);

            // Validate response shape
            JsonNode blocks = GeminiResponseValidator.validateVoiceSegmentation(rawResponse);

            // Build parsed work blocks
            List<WorkBlock> workBlocks = new ArrayList<>();
            for (JsonNode b : blocks) {
                WorkBlock wb = new WorkBlock();
                wb.setTitle(b.path("title").asText());
                wb.setCategory(b.path("category").asText());
                wb.setDurationHours(b.path("durationHours").asDouble());
                wb.setDescription(b.path("description").asText());
                wb.setOriginalTranscriptFragment(transcript); // full transcript shown alongside
                String matchedTask = b.path("matchedTaskTitle").asText(null);
                if (matchedTask != null) {
                    // Find matching task ID
                    assignedTasks.stream()
                        .filter(t -> t.getTitle().equalsIgnoreCase(matchedTask))
                        .findFirst()
                        .ifPresent(t -> { wb.setMatchedTaskId(t.getId()); wb.setMatchedTaskTitle(t.getTitle()); });
                }
                workBlocks.add(wb);
            }

            // Save success
            voiceRecordDAO.updateParsedJson(recordId, rawResponse, "SUCCESS");

            return ParseResult.success(recordId, workBlocks, transcript);

        } catch (GeminiException | GeminiValidationException e) {
            log.error("[{}] Voice AI processing failed for user {}: {}", correlationId, userId, e.getMessage());
            voiceRecordDAO.updateStatus(recordId, "FAILED", e.getMessage());
            voiceRecordDAO.incrementRetryCount(recordId);
            return ParseResult.failure(recordId, transcript, "AI processing failed. Your transcript has been saved. You can retry or enter manually.");
        }
    }

    /**
     * Retry a failed/draft voice record.
     */
    public ParseResult retry(int recordId, int userId) {
        Optional<VoiceRecord> found = voiceRecordDAO.findById(recordId);
        if (found.isEmpty() || found.get().getUserId() != userId) {
            return ParseResult.failure(recordId, null, "Record not found.");
        }
        VoiceRecord record = found.get();
        if (!record.canRetry()) {
            return ParseResult.failure(recordId, record.getTranscript(), "Maximum retries reached. Please enter manually.");
        }
        voiceRecordDAO.updateStatus(recordId, "PROCESSING", null);
        voiceRecordDAO.incrementRetryCount(recordId);
        return processTranscript(userId, record.getTranscript(), record.getAudioFileRef());
    }

    /**
     * Step 5: Employee has reviewed and confirmed blocks — save to timesheet_entries.
     */
    public List<TimesheetEntry> confirmAndSave(int userId, int voiceRecordId,
                                                List<WorkBlock> confirmedBlocks, LocalDate entryDate) {
        List<TimesheetEntry> saved = new ArrayList<>();
        for (WorkBlock wb : confirmedBlocks) {
            TimesheetEntry te = new TimesheetEntry();
            te.setUserId(userId); te.setVoiceRecordId(voiceRecordId);
            te.setEntryDate(entryDate != null ? entryDate : LocalDate.now());
            te.setTitle(wb.getTitle()); te.setDescription(wb.getDescription());
            te.setDurationHours(wb.getDurationHours()); te.setConfirmed(true);
            if (wb.getMatchedTaskId() != null) te.setTaskId(wb.getMatchedTaskId());
            // category lookup by name (simplified)
            int id = timesheetDAO.insert(te);
            te.setId(id);
            saved.add(te);

            // Rollup actual hours on task if linked
            if (wb.getMatchedTaskId() != null) {
                double hours = timesheetDAO.sumHoursByTaskId(wb.getMatchedTaskId());
                taskDAO.updateActualHours(wb.getMatchedTaskId(), hours);
            }
        }
        return saved;
    }

    public List<VoiceRecord> getDrafts(int userId) { return voiceRecordDAO.findDraftsByUserId(userId); }

    // ====== Inner classes ======

    public static class WorkBlock {
        private String title, category, description, originalTranscriptFragment, matchedTaskTitle;
        private double durationHours;
        private Integer matchedTaskId;

        public String getTitle() { return title; }
        public void setTitle(String t) { this.title = t; }
        public String getCategory() { return category; }
        public void setCategory(String c) { this.category = c; }
        public String getDescription() { return description; }
        public void setDescription(String d) { this.description = d; }
        public String getOriginalTranscriptFragment() { return originalTranscriptFragment; }
        public void setOriginalTranscriptFragment(String o) { this.originalTranscriptFragment = o; }
        public String getMatchedTaskTitle() { return matchedTaskTitle; }
        public void setMatchedTaskTitle(String m) { this.matchedTaskTitle = m; }
        public double getDurationHours() { return durationHours; }
        public void setDurationHours(double h) { this.durationHours = h; }
        public Integer getMatchedTaskId() { return matchedTaskId; }
        public void setMatchedTaskId(Integer id) { this.matchedTaskId = id; }
    }

    public static class ParseResult {
        private final boolean success;
        private final int recordId;
        private final List<WorkBlock> workBlocks;
        private final String transcript;
        private final String errorMessage;

        private ParseResult(boolean success, int recordId, List<WorkBlock> workBlocks,
                             String transcript, String errorMessage) {
            this.success = success; this.recordId = recordId;
            this.workBlocks = workBlocks; this.transcript = transcript; this.errorMessage = errorMessage;
        }

        public static ParseResult success(int recordId, List<WorkBlock> blocks, String transcript) {
            return new ParseResult(true, recordId, blocks, transcript, null);
        }

        public static ParseResult failure(int recordId, String transcript, String error) {
            return new ParseResult(false, recordId, List.of(), transcript, error);
        }

        public boolean isSuccess()          { return success; }
        public int getRecordId()            { return recordId; }
        public List<WorkBlock> getWorkBlocks() { return workBlocks; }
        public String getTranscript()       { return transcript; }
        public String getErrorMessage()     { return errorMessage; }
    }
}
