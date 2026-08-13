package com.taskvoice.controller.manager;

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

@WebServlet("/manager/reports")
public class ReportManagerServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String format  = req.getParameter("format");
        String fromStr = req.getParameter("from");
        String toStr   = req.getParameter("to");
        String empIdStr = req.getParameter("employeeId");

        if (format == null) {
            req.getRequestDispatcher("/WEB-INF/views/manager/reports.jsp").forward(req, resp);
            return;
        }

        User manager = SessionUtil.getUser(req.getSession(false));
        LocalDate to   = (toStr != null && !toStr.isBlank()) ? LocalDate.parse(toStr) : LocalDate.now();
        LocalDate from = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr) : to.minusDays(30);

        Integer targetUserId = manager.getId();
        if (empIdStr != null && !empIdStr.isBlank()) {
            try { targetUserId = Integer.parseInt(empIdStr); } catch (NumberFormatException ignored) {}
        }

        try {
            if ("csv".equalsIgnoreCase(format)) {
                byte[] data;
                if (empIdStr != null && !empIdStr.isBlank()) {
                    data = reportService.generateCsvReport(targetUserId, from, to);
                } else {
                    data = reportService.generateTeamCsvReport(manager.getId(), from, to);
                }
                resp.setContentType("text/csv");
                resp.setHeader("Content-Disposition", "attachment; filename=\"taskvoice_timesheet_report_" + from + "_to_" + to + ".csv\"");
                resp.getOutputStream().write(data);
            } else if ("pdf".equalsIgnoreCase(format)) {
                byte[] data = reportService.generatePdfReport(targetUserId, from, to);
                resp.setContentType("application/pdf");
                resp.setHeader("Content-Disposition", "attachment; filename=\"taskvoice_work_statement_" + from + "_to_" + to + ".pdf\"");
                resp.getOutputStream().write(data);
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error generating report: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/manager/reports.jsp").forward(req, resp);
        }
    }
}
