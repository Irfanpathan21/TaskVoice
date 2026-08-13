package com.taskvoice.controller.admin;

import com.taskvoice.dao.AuditLogDAO;
import com.taskvoice.dao.impl.AuditLogDAOImpl;
import com.taskvoice.model.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/audit-logs")
public class AuditLogServlet extends HttpServlet {

    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page = 1;
        String pStr = req.getParameter("page");
        if (pStr != null && !pStr.isBlank()) {
            try { page = Math.max(1, Integer.parseInt(pStr)); } catch (NumberFormatException ignored) {}
        }
        String actionFilter = req.getParameter("actionFilter");
        if (actionFilter != null && actionFilter.isBlank()) actionFilter = null;

        int pageSize = 30;
        List<AuditLog> logs = auditDAO.findAll(page, pageSize, actionFilter);
        int total = auditDAO.countAll(actionFilter);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("logs", logs);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("actionFilter", actionFilter);

        req.getRequestDispatcher("/WEB-INF/views/admin/audit-logs.jsp").forward(req, resp);
    }
}
