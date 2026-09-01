package com.taskvoice.controller.employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.taskvoice.dao.CategoryDAO;
import com.taskvoice.dao.impl.CategoryDAOImpl;
import com.taskvoice.model.User;
import com.taskvoice.service.TaskService;
import com.taskvoice.service.VoiceService;
import com.taskvoice.service.VoiceService.ParseResult;
import com.taskvoice.service.VoiceService.WorkBlock;
import com.taskvoice.util.JsonUtil;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/employee/voice-timesheet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB threshold
    maxFileSize = 1024 * 1024 * 25,        // 25 MB max file size
    maxRequestSize = 1024 * 1024 * 30      // 30 MB max request size
)
public class VoiceTimesheetServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(VoiceTimesheetServlet.class);

    private final VoiceService voiceService = new VoiceService();
    private final TaskService  taskService  = new TaskService();
    private final CategoryDAO  categoryDAO  = new CategoryDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));

        if (employee == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            req.setAttribute("assignedTasks", taskService.findByAssignee(employee.getId(), 1, 100));
            req.setAttribute("categories", categoryDAO.findAll());
            req.setAttribute("drafts", voiceService.getDrafts(employee.getId()));

            req.getRequestDispatcher("/WEB-INF/views/employee/voice-timesheet.jsp").forward(req, resp);
        } catch (Exception e) {
            log.error("Failed to load voice timesheet page: {}", e.getMessage(), e);
            req.setAttribute("errorMessage", "Failed to load voice timesheet: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        User employee = SessionUtil.getUser(req.getSession(false));
        if (employee == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Session expired. Please log in again."));
            return;
        }

        try {
            String action = getFormOrPartValue(req, "action");
            if (action == null || action.isBlank()) {
                action = "process";
            }

            if ("process".equals(action)) {
                String transcript  = getFormOrPartValue(req, "transcript");
                String mimeType    = getFormOrPartValue(req, "mimeType");
                String audioBase64 = getFormOrPartValue(req, "audioBase64");

                byte[] audioBytes = null;

                // 1. Check if a binary audio file was uploaded via multipart FormData
                try {
                    Part audioPart = req.getPart("audioFile");
                    if (audioPart != null && audioPart.getSize() > 0) {
                        try (InputStream is = audioPart.getInputStream()) {
                            audioBytes = is.readAllBytes();
                        }
                        if (mimeType == null || mimeType.isBlank()) {
                            mimeType = audioPart.getContentType();
                        }
                    }
                } catch (Exception ignored) {
                    // Not multipart or no audioFile part
                }

                // 2. Fall back to audioBase64 if binary audio part was not provided
                if ((audioBytes == null || audioBytes.length == 0) && audioBase64 != null && !audioBase64.isBlank()) {
                    try {
                        String cleanBase64 = audioBase64.contains(",") ? audioBase64.split(",")[1] : audioBase64;
                        audioBytes = java.util.Base64.getDecoder().decode(cleanBase64.trim());
                    } catch (Exception e) {
                        // Fall back to transcript
                    }
                }

                if ((transcript == null || transcript.isBlank()) && (audioBytes == null || audioBytes.length == 0)) {
                    resp.getWriter().write(JsonUtil.error("Please speak into the microphone or enter a text recap."));
                    return;
                }

                ParseResult result = voiceService.processAudioData(employee.getId(), audioBytes, mimeType, transcript);
                resp.getWriter().write(JsonUtil.toJson(result));

            } else if ("retry".equals(action)) {
                String recIdStr = getFormOrPartValue(req, "recordId");
                int recordId = 0;
                if (recIdStr != null && !recIdStr.isBlank()) {
                    try { recordId = Integer.parseInt(recIdStr); } catch (Exception ignored) {}
                }
                ParseResult result = voiceService.retry(recordId, employee.getId());
                resp.getWriter().write(JsonUtil.toJson(result));

            } else if ("confirm".equals(action)) {
                String recIdStr = getFormOrPartValue(req, "recordId");
                int recordId = 0;
                if (recIdStr != null && !recIdStr.isBlank()) {
                    try { recordId = Integer.parseInt(recIdStr); } catch (Exception ignored) {}
                }

                String blocksJson = getFormOrPartValue(req, "blocksJson");
                String dateStr    = getFormOrPartValue(req, "entryDate");
                LocalDate date    = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();

                List<WorkBlock> blocks = parseBlocksJson(blocksJson);
                if (blocks.isEmpty()) {
                    resp.getWriter().write(JsonUtil.error("Please provide at least one valid timesheet entry."));
                    return;
                }

                voiceService.confirmAndSave(employee.getId(), recordId, blocks, date);

                req.getSession().setAttribute("flashMessage", "Timesheet entries confirmed and saved successfully.");
                resp.getWriter().write(JsonUtil.ok("Saved successfully"));
            } else {
                resp.getWriter().write(JsonUtil.error("Unknown action: " + action));
            }
        } catch (Exception e) {
            log.error("Error processing voice timesheet action: {}", e.getMessage(), e);
            resp.getWriter().write(JsonUtil.error("Server error: " + e.getMessage()));
        }
    }

    private String getFormOrPartValue(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        if (val != null) return val;
        try {
            Part part = req.getPart(name);
            if (part != null) {
                try (InputStream is = part.getInputStream()) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private List<WorkBlock> parseBlocksJson(String json) {
        List<WorkBlock> blocks = new ArrayList<>();
        if (json == null || json.isBlank()) return blocks;

        JsonUtil.parse(json).ifPresent(node -> {
            if (node.isArray()) {
                for (JsonNode n : node) {
                    WorkBlock wb = new WorkBlock();
                    String title = n.path("title").asText("").trim();
                    wb.setTitle(title.isEmpty() ? "Work Entry" : title);
                    wb.setCategory(n.path("category").asText("Development"));
                    if (n.has("categoryId") && !n.path("categoryId").isNull() && n.path("categoryId").asInt(0) > 0) {
                        wb.setCategoryId(n.path("categoryId").asInt());
                    }
                    double dur = n.path("durationHours").asDouble(1.0);
                    wb.setDurationHours(dur > 0 ? dur : 1.0);
                    wb.setDescription(n.path("description").asText(""));
                    if (n.has("matchedTaskId") && !n.path("matchedTaskId").isNull() && n.path("matchedTaskId").asInt(0) > 0) {
                        wb.setMatchedTaskId(n.path("matchedTaskId").asInt());
                    }
                    blocks.add(wb);
                }
            }
        });
        return blocks;
    }
}
