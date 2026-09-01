package com.taskvoice.filter;

import com.taskvoice.model.User;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * RoleFilter — enforces role-based access control on protected requests.
 * Runs BEFORE any servlet logic. Redirects to login/forbidden on violation.
 * 
 * Hierarchy (checks both roleId and roleName):
 * - ADMIN (roleId 1): full access to all routes
 * - MANAGER (roleId 2): access to /manager/*, /employee/*, /shared/*
 * - EMPLOYEE (roleId 3): access to /employee/*, /shared/*
 */
@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/employee/*", "/shared/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        User user = SessionUtil.getUser(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();
        int roleId = user.getRoleId();
        String roleName = user.getRoleName() != null ? user.getRoleName().toUpperCase() : "";

        boolean allowed = false;

        if (roleId == 1 || "ADMIN".equals(roleName)) {
            // ADMIN has full system access
            allowed = true;
        } else if ((roleId == 2 || "MANAGER".equals(roleName)) && (path.startsWith("/manager/") || path.startsWith("/employee/") || path.startsWith("/shared/"))) {
            // MANAGER has access to manager, employee, and shared routes
            allowed = true;
        } else if ((roleId == 3 || "EMPLOYEE".equals(roleName)) && (path.startsWith("/employee/") || path.startsWith("/shared/"))) {
            // EMPLOYEE has access to employee and shared routes
            allowed = true;
        } else if (path.startsWith("/shared/")) {
            allowed = true;
        }

        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Insufficient permissions");
            return;
        }

        chain.doFilter(req, res);
    }
}
