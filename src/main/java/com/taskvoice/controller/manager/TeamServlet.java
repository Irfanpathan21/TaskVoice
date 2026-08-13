package com.taskvoice.controller.manager;

import com.taskvoice.model.User;
import com.taskvoice.service.AppraisalService;
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
import java.util.List;
import java.util.Optional;

@WebServlet("/manager/team")
public class TeamServlet extends HttpServlet {

    private final EmployeeService  empService       = new EmployeeService();
    private final ProjectService   projectService   = new ProjectService();
    private final TaskService      taskService      = new TaskService();
    private final TimesheetService timesheetService = new TimesheetService();
    private final AppraisalService appraisalService = new AppraisalService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isBlank()) {
            int empId = Integer.parseInt(idStr);
            if (!empService.isMyEmployee(manager.getId(), empId)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            Optional<User> opt = empService.findById(empId);
            if (opt.isEmpty()) { resp.sendError(HttpServletResponse.SC_NOT_FOUND); return; }

            User employee = opt.get();
            req.setAttribute("employee", employee);
            req.setAttribute("projects", projectService.findByEmployee(empId, 1, 100));
            req.setAttribute("tasks", taskService.findByAssignee(empId, 1, 100));
            req.setAttribute("appraisals", appraisalService.findByEmployee(empId));

            LocalDate now = LocalDate.now();
            req.setAttribute("loggedHoursThisMonth", timesheetService.getRangeHours(empId, now.withDayOfMonth(1), now));

            req.getRequestDispatcher("/WEB-INF/views/manager/employee-detail.jsp").forward(req, resp);
            return;
        }

        List<User> team = empService.findTeam(manager.getId());
        req.setAttribute("team", team);
        req.getRequestDispatcher("/WEB-INF/views/manager/team.jsp").forward(req, resp);
    }
}
