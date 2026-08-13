package com.taskvoice.controller.employee;

import com.taskvoice.model.User;
import com.taskvoice.service.ReportService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/employee/reports")
public class ReportEmployeeServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String format  = req.getParameter("format");
        String fromStr = req.getParameter("from");
        String toStr   = req.getParameter("to");

        if (format == null) {
            req.getRequestDispatcher("/WEB-INF/views/employee/reports.jsp").forward(req, resp);
            return;
        }

        User employee = SessionUtil.getUser(req.getSession(false));
        LocalDate to   = (toStr != null && !toStr.isBlank()) ? LocalDate.parse(toStr) : LocalDate.now();
        LocalDate from = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr) : to.minusDays(30);

        try {
            if ("csv".equalsIgnoreCase(format)) {
                byte[] data = reportService.generateCsvReport(employee.getId(), from, to);
                resp.setContentType("text/csv");
                resp.setHeader("Content-Disposition", "attachment; filename=\"my_timesheet_" + from + "_to_" + to + ".csv\"");
                resp.getOutputStream().write(data);
            } else if ("pdf".equalsIgnoreCase(format)) {
                byte[] data = reportService.generatePdfReport(employee.getId(), from, to);
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=\"my_work_statement_" + from + "_to_" + to + ".pdf\"");
                resp.getOutputStream().write(data);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error generating report: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/employee/reports.jsp").forward(req, resp);
        }
    }
}
