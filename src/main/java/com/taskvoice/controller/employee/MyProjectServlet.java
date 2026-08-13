package com.taskvoice.controller.employee;

import com.taskvoice.model.Project;
import com.taskvoice.model.User;
import com.taskvoice.service.ProjectService;
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

@WebServlet("/employee/projects")
public class MyProjectServlet extends HttpServlet {

    private final ProjectService projectService = new ProjectService();
    private final TaskService    taskService    = new TaskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int projectId = Integer.parseInt(idStr);
            Optional<Project> opt = projectService.findById(projectId);
            if (opt.isEmpty()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            Project p = opt.get();
            req.setAttribute("project", p);
            req.setAttribute("tasks", taskService.findByProject(projectId));
            req.getRequestDispatcher("/WEB-INF/views/employee/project-detail.jsp").forward(req, resp);
            return;
        }

        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        int pageSize = 15;
        List<Project> projects = projectService.findByEmployee(employee.getId(), page, pageSize);
        int total = projectService.countByEmployee(employee.getId());
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("projects", projects);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);

        req.getRequestDispatcher("/WEB-INF/views/employee/projects.jsp").forward(req, resp);
    }
}
