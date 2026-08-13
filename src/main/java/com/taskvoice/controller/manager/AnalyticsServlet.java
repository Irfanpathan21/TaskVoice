package com.taskvoice.controller.manager;

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
import java.util.List;

@WebServlet("/manager/analytics")
public class AnalyticsServlet extends HttpServlet {

    private final EmployeeService empService = new EmployeeService();
    private final ProjectService  projectService = new ProjectService();
    private final TaskService     taskService = new TaskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        int mgId = manager.getId();

        List<User> team = empService.findTeam(mgId);
        req.setAttribute("team", team);
        req.setAttribute("projects", projectService.findByManager(mgId, 1, 100));
        req.setAttribute("overdueCount", taskService.countOverdue(mgId));
        req.setAttribute("totalTasks", taskService.countByManagerTeam(mgId));

        req.getRequestDispatcher("/WEB-INF/views/manager/analytics.jsp").forward(req, resp);
    }
}
