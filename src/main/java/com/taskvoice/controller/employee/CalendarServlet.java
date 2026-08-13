package com.taskvoice.controller.employee;

import com.taskvoice.model.TimesheetEntry;
import com.taskvoice.model.User;
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

@WebServlet("/employee/calendar")
public class CalendarServlet extends HttpServlet {

    private final TimesheetService timesheetService = new TimesheetService();
    private final TaskService      taskService      = new TaskService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User employee = SessionUtil.getUser(req.getSession(false));
        String dateStr = req.getParameter("date");

        LocalDate date = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : LocalDate.now();

        List<TimesheetEntry> entries = timesheetService.getForDate(employee.getId(), date);
        double totalHours = timesheetService.getTodayHours(employee.getId());

        req.setAttribute("selectedDate", date);
        req.setAttribute("entries", entries);
        req.setAttribute("totalHours", totalHours);
        req.setAttribute("dueSoonTasks", taskService.findDueSoon(employee.getId(), 14));

        req.getRequestDispatcher("/WEB-INF/views/employee/calendar.jsp").forward(req, resp);
    }
}
