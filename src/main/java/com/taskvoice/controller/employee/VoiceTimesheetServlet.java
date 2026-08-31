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
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/employee/voice-timesheet")
public class VoiceTimesheetServlet extends HttpServlet {

    private final VoiceService voiceService = new VoiceService();
    private final TaskService  taskService  = new TaskService();
    private final CategoryDAO  categoryDAO  = new CategoryDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));

        req.setAttribute("assignedTasks", taskService.findByAssignee(employee.getId(), 1, 100));
        req.setAttribute("categories", categoryDAO.findAll());
        req.setAttribute("drafts", voiceService.getDrafts(employee.getId()));

        req.getRequestDispatcher("/WEB-INF/views/employee/voice-timesheet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");
        resp.setContentType("application/json");

        if ("process".equals(action)) {
            String transcript  = req.getParameter("transcript");
            String audioBase64 = req.getParameter("audioBase64");
            String mimeType    = req.getParameter("mimeType");

            byte[] audioBytes = null;
            if (audioBase64 != null && !audioBase64.isBlank()) {
                try {
                    String cleanBase64 = audioBase64.contains(",") ? audioBase64.split(",")[1] : audioBase64;
                    audioBytes = java.util.Base64.getDecoder().decode(cleanBase64.trim());
                } catch (Exception e) {
                    // Fall back to text transcript
                }
            }

            if ((transcript == null || transcript.isBlank()) && (audioBytes == null || audioBytes.length == 0)) {
                resp.getWriter().write(JsonUtil.error("Please record speech or enter a text recap."));
                return;
            }

            ParseResult result = voiceService.processAudioData(employee.getId(), audioBytes, mimeType, transcript);
            resp.getWriter().write(JsonUtil.toJson(result));

        } else if ("retry".equals(action)) {
            int recordId = Integer.parseInt(req.getParameter("recordId"));
            ParseResult result = voiceService.retry(recordId, employee.getId());
            resp.getWriter().write(JsonUtil.toJson(result));

        } else if ("confirm".equals(action)) {
            int recordId = Integer.parseInt(req.getParameter("recordId"));
            String blocksJson = req.getParameter("blocksJson");
            String dateStr    = req.getParameter("entryDate");
            LocalDate date    = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();

            List<WorkBlock> blocks = parseBlocksJson(blocksJson);
            voiceService.confirmAndSave(employee.getId(), recordId, blocks, date);

            req.getSession().setAttribute("flashMessage", "Timesheet entries confirmed and saved.");
            resp.getWriter().write(JsonUtil.ok("Saved successfully"));
        }
    }

    private List<WorkBlock> parseBlocksJson(String json) {
        List<WorkBlock> blocks = new ArrayList<>();
        JsonUtil.parse(json).ifPresent(node -> {
            if (node.isArray()) {
                for (JsonNode n : node) {
                    WorkBlock wb = new WorkBlock();
                    wb.setTitle(n.path("title").asText());
                    wb.setCategory(n.path("category").asText());
                    wb.setDurationHours(n.path("durationHours").asDouble(1.0));
                    wb.setDescription(n.path("description").asText());
                    if (n.has("matchedTaskId") && !n.path("matchedTaskId").isNull()) {
                        wb.setMatchedTaskId(n.path("matchedTaskId").asInt());
                    }
                    blocks.add(wb);
                }
            }
        });
        return blocks;
    }
}
