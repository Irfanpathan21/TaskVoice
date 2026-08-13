package com.taskvoice.controller.manager;

import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.service.ProjectService;
import com.taskvoice.service.TaskService;
import com.taskvoice.service.TimesheetService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/manager/dashboard")
public class ManagerDashboardServlet extends HttpServlet {

    private final EmployeeService empService = new EmployeeService();
    private final ProjectService  projectService = new ProjectService();
    private final TaskService     taskService = new TaskService();
    private final TimesheetService timesheetService = new TimesheetService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        int mgId = manager.getId();

        req.setAttribute("teamSize",           empService.findTeam(mgId).size());
        req.setAttribute("activeProjectsCount", projectService.countByManager(mgId));
        req.setAttribute("totalTasksCount",     taskService.countByManagerTeam(mgId));
        req.setAttribute("overdueTasksCount",   taskService.countOverdue(mgId));
        req.setAttribute("pendingGradesCount",  taskService.countPendingGrades(mgId));

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        req.setAttribute("loggedHoursThisMonth", timesheetService.getTeamForRange(mgId, monthStart, now)
                .stream().mapToDouble(t -> t.getDurationHours()).sum());

        req.setAttribute("overdueTasks", taskService.findOverdue(mgId));
        req.setAttribute("recentProjects", projectService.findByManager(mgId, 1, 5));

        req.getRequestDispatcher("/WEB-INF/views/manager/dashboard.jsp").forward(req, resp);
    }
}
