package com.taskvoice.controller.manager;

import com.taskvoice.model.TimesheetEntry;
import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
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

@WebServlet("/manager/timesheets")
public class TimesheetManagerServlet extends HttpServlet {

    private final TimesheetService timesheetService = new TimesheetService();
    private final EmployeeService  empService       = new EmployeeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User manager = SessionUtil.getUser(req.getSession(false));
        String fromStr = req.getParameter("from");
        String toStr   = req.getParameter("to");
        String empIdStr = req.getParameter("employeeId");

        LocalDate to   = (toStr != null && !toStr.isBlank()) ? LocalDate.parse(toStr) : LocalDate.now();
        LocalDate from = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr) : to.minusDays(30);

        Integer selectedEmpId = null;
        if (empIdStr != null && !empIdStr.isBlank()) {
            try { selectedEmpId = Integer.parseInt(empIdStr); } catch (NumberFormatException ignored) {}
        }

        List<TimesheetEntry> entries;
        if (selectedEmpId != null) {
            entries = timesheetService.getForRange(selectedEmpId, from, to);
        } else {
            entries = timesheetService.getTeamForRange(manager.getId(), from, to);
        }

        req.setAttribute("entries", entries);
        req.setAttribute("fromDate", from);
        req.setAttribute("toDate", to);
        req.setAttribute("selectedEmployeeId", selectedEmpId);
        req.setAttribute("team", empService.findTeam(manager.getId()));

        req.getRequestDispatcher("/WEB-INF/views/manager/timesheets.jsp").forward(req, resp);
    }
}
