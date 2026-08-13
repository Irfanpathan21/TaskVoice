package com.taskvoice.model;

import java.time.LocalDateTime;

public class VoiceRecord {
    private int id;
    private int userId;
    private String audioFileRef;
    private String transcript;
    private String aiParsedJson;
    private String processingStatus; // PENDING, PROCESSING, SUCCESS, FAILED, DRAFT
    private String errorMessage;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VoiceRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAudioFileRef() { return audioFileRef; }
    public void setAudioFileRef(String audioFileRef) { this.audioFileRef = audioFileRef; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public String getAiParsedJson() { return aiParsedJson; }
    public void setAiParsedJson(String aiParsedJson) { this.aiParsedJson = aiParsedJson; }

    public String getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDraft()   { return "DRAFT".equals(processingStatus); }
    public boolean isFailed()  { return "FAILED".equals(processingStatus); }
    public boolean isSuccess() { return "SUCCESS".equals(processingStatus); }
    public boolean canRetry()  { return (isDraft() || isFailed()) && retryCount < 3; }
}
