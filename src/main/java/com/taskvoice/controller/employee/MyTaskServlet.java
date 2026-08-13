package com.taskvoice.controller.employee;

import com.taskvoice.model.Task;
import com.taskvoice.model.User;
import com.taskvoice.service.GeminiService;
import com.taskvoice.service.TaskService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/employee/tasks")
public class MyTaskServlet extends HttpServlet {

    private final TaskService   taskService   = new TaskService();
    private final GeminiService geminiService = new GeminiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int taskId = Integer.parseInt(idStr);
            if (!taskService.isMyTask(taskId, employee.getId())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Optional<Task> opt = taskService.findById(taskId);
            if (opt.isEmpty()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            req.setAttribute("task", opt.get());
            req.setAttribute("updates", taskService.getUpdates(taskId));
            req.getRequestDispatcher("/WEB-INF/views/employee/task-detail.jsp").forward(req, resp);
            return;
        }

        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        int pageSize = 20;
        List<Task> tasks = taskService.findByAssignee(employee.getId(), page, pageSize);
        int total = taskService.countByAssignee(employee.getId());
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("tasks", tasks);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/WEB-INF/views/employee/tasks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");

        try {
            if ("addUpdate".equals(action)) {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                if (!taskService.isMyTask(taskId, employee.getId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                String rawText = req.getParameter("rawText");
                Integer completionPct = parseInteger(req.getParameter("completionPct"));
                String problemsFaced = req.getParameter("problemsFaced");
                String note = req.getParameter("note");

                // Generate professional AI rephrasing (Prompt 2), preserving raw text
                String aiRephrased = geminiService.rephrase(rawText);

                taskService.addProgressUpdate(taskId, employee.getId(), rawText, completionPct,
                        problemsFaced, note, null, aiRephrased);

                req.getSession().setAttribute("flashMessage", "Progress update submitted.");
                resp.sendRedirect(req.getContextPath() + "/employee/tasks?id=" + taskId);
                return;

            } else if ("updateStatus".equals(action)) {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                if (!taskService.isMyTask(taskId, employee.getId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                String status = req.getParameter("status");
                // Employee cannot cancel
                if (!"CANCELLED".equals(status)) {
                    taskService.updateStatus(taskId, status, employee.getId(), employee.getName(), req.getRemoteAddr());
                    req.getSession().setAttribute("flashMessage", "Task status updated.");
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/employee/tasks");
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) return null;
        try { return Integer.parseInt(str); } catch (NumberFormatException e) { return null; }
    }
}
