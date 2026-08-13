package com.taskvoice.controller.manager;

import com.taskvoice.model.Project;
import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.service.GeminiService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/manager/projects")
public class ProjectServlet extends HttpServlet {

    private final ProjectService  projectService = new ProjectService();
    private final EmployeeService empService     = new EmployeeService();
    private final TaskService     taskService    = new TaskService();
    private final GeminiService   geminiService  = new GeminiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int projectId = Integer.parseInt(idStr);
            Optional<Project> opt = projectService.findById(projectId);
            if (opt.isEmpty() || opt.get().getManagerId() != manager.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Project p = opt.get();
            req.setAttribute("project", p);
            req.setAttribute("tasks", taskService.findByProject(projectId));
            req.setAttribute("team", empService.findTeam(manager.getId()));
            req.getRequestDispatcher("/WEB-INF/views/manager/project-detail.jsp").forward(req, resp);
            return;
        }

        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        int pageSize = 15;
        List<Project> projects = projectService.findByManager(manager.getId(), page, pageSize);
        int total = projectService.countByManager(manager.getId());
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("projects", projects);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("team", empService.findTeam(manager.getId()));

        req.getRequestDispatcher("/WEB-INF/views/manager/projects.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                LocalDate startDate = LocalDate.parse(req.getParameter("startDate"));
                LocalDate endDate = LocalDate.parse(req.getParameter("endDate"));
                String[] memberIdsArr = req.getParameterValues("memberIds");

                List<Integer> memberIds = List.of();
                if (memberIdsArr != null) {
                    memberIds = Arrays.stream(memberIdsArr).map(Integer::parseInt).collect(Collectors.toList());
                }

                Project p = projectService.createProject(title, description, manager.getId(),
                        startDate, endDate, memberIds, manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "Project created successfully.");

            } else if ("update".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                String status = req.getParameter("status");
                LocalDate startDate = LocalDate.parse(req.getParameter("startDate"));
                LocalDate endDate = LocalDate.parse(req.getParameter("endDate"));

                projectService.updateProject(id, title, description, status, startDate, endDate,
                        manager.getId(), manager.getName(), req.getRemoteAddr());

                req.getSession().setAttribute("flashMessage", "Project updated.");

            } else if ("complete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                projectService.completeProject(id, manager.getId(), manager.getName(), req.getRemoteAddr());

                // Trigger AI Sentiment & Summary analysis in background/inline
                Optional<Project> opt = projectService.findById(id);
                opt.ifPresent(geminiService::generateProjectAnalysis);

                req.getSession().setAttribute("flashMessage", "Project completed and AI Analysis generated.");

            } else if ("addMember".equals(action)) {
                int projectId = Integer.parseInt(req.getParameter("projectId"));
                int userId = Integer.parseInt(req.getParameter("userId"));
                projectService.addMember(projectId, userId, manager.getId(), manager.getName(), req.getRemoteAddr());
                req.getSession().setAttribute("flashMessage", "Team member added to project.");
            }
        } catch (Exception e) {
            req.getSession().setAttribute("flashError", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/manager/projects");
    }
}
