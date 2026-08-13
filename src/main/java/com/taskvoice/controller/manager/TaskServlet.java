package com.taskvoice.controller.manager;

import com.taskvoice.dao.CategoryDAO;
import com.taskvoice.dao.impl.CategoryDAOImpl;
import com.taskvoice.model.Task;
import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.service.ProjectService;
import com.taskvoice.service.TaskService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet("/manager/tasks")
public class TaskServlet extends HttpServlet {

    private final TaskService     taskService    = new TaskService();
    private final ProjectService  projectService = new ProjectService();
    private final EmployeeService empService     = new EmployeeService();
    private final CategoryDAO     categoryDAO    = new CategoryDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int taskId = Integer.parseInt(idStr);
            Optional<Task> opt = taskService.findById(taskId);
            if (opt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Task task = opt.get();
            req.setAttribute("task", task);
            req.setAttribute("updates", taskService.getUpdates(taskId));
            req.getRequestDispatcher("/WEB-INF/views/manager/task-detail.jsp").forward(req, resp);
            return;
        }

        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        int pageSize = 20;
        List<Task> tasks = taskService.findByManagerTeam(manager.getId(), page, pageSize);
        int total = taskService.countByManagerTeam(manager.getId());
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("tasks", tasks);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("projects", projectService.findByManager(manager.getId(), 1, 100));
        req.setAttribute("team", empService.findTeam(manager.getId()));
        req.setAttribute("categories", categoryDAO.findAll());

        req.getRequestDispatcher("/WEB-INF/views/manager/tasks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                int projectId = Integer.parseInt(req.getParameter("projectId"));
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                int assigneeId = Integer.parseInt(req.getParameter("assigneeId"));
                Integer categoryId = parseInteger(req.getParameter("categoryId"));
                String priority = req.getParameter("priority");
                LocalDate startDate = LocalDate.parse(req.getParameter("startDate"));
                LocalDate dueDate = LocalDate.parse(req.getParameter("dueDate"));
                double expectedHours = Double.parseDouble(req.getParameter("expectedHours"));

                taskService.createTask(projectId, title, description, assigneeId, categoryId, priority,
                        startDate, dueDate, expectedHours, manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "Task created and assigned successfully.");

            } else if ("grade".equals(action)) {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                String grade = req.getParameter("grade");
                double score = Double.parseDouble(req.getParameter("score"));
                String remark = req.getParameter("remark");

                taskService.gradeTask(taskId, grade, score, remark, manager.getId(), manager.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Task graded successfully.");

            } else if ("updateStatus".equals(action)) {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                String status = req.getParameter("status");

                taskService.updateStatus(taskId, status, manager.getId(), manager.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Task status updated.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/manager/tasks");
    }

    private Integer parseInteger(String str) {
        if (str == null || str.isBlank()) return null;
        try { return Integer.parseInt(str); } catch (NumberFormatException e) { return null; }
    }
}
