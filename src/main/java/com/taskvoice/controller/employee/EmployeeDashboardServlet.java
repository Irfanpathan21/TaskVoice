package com.taskvoice.controller.employee;

import com.taskvoice.model.User;
import com.taskvoice.service.AppraisalService;
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

@WebServlet("/employee/dashboard")
public class EmployeeDashboardServlet extends HttpServlet {

    private final ProjectService   projectService   = new ProjectService();
    private final TaskService      taskService      = new TaskService();
    private final TimesheetService timesheetService = new TimesheetService();
    private final AppraisalService appraisalService = new AppraisalService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        int empId = employee.getId();

        req.setAttribute("myProjectsCount", projectService.countByEmployee(empId));
        req.setAttribute("myTasksCount",    taskService.countByAssignee(empId));
        req.setAttribute("dueSoonTasks",    taskService.findDueSoon(empId, 7));
        req.setAttribute("todayHours",      timesheetService.getTodayHours(empId));
        req.setAttribute("recentAppraisals",appraisalService.findByEmployee(empId));
        req.setAttribute("myTasks",         taskService.findByAssignee(empId, 1, 5));

        req.getRequestDispatcher("/WEB-INF/views/employee/dashboard.jsp").forward(req, resp);
    }
}
