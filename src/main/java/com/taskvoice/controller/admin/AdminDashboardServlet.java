package com.taskvoice.controller.admin;

import com.taskvoice.dao.DepartmentDAO;
import com.taskvoice.dao.impl.DepartmentDAOImpl;
import com.taskvoice.model.User;
import com.taskvoice.service.EmployeeService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final EmployeeService empService = new EmployeeService();
    private final DepartmentDAO   deptDAO    = new DepartmentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User actor = SessionUtil.getUser(req.getSession(false));

        req.setAttribute("totalEmployees",  empService.countByRole("EMPLOYEE"));
        req.setAttribute("totalManagers",   empService.countByRole("MANAGER"));
        req.setAttribute("departments",     empService.findAllDepartments());
        req.setAttribute("recentAuditLogs", new com.taskvoice.dao.impl.AuditLogDAOImpl().findAll(1, 10, null));

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
