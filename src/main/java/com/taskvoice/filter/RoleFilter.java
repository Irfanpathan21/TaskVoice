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
 * RoleFilter — enforces role-based access control on every protected request.
 * Runs BEFORE any servlet logic. Redirects to 403 on violation.
 * Never trusts role values from the client.
 */
@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/employee/*"})
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
        String role = user.getRoleName();

        boolean allowed = false;

        if (path.startsWith("/admin/") && "ADMIN".equals(role)) {
            allowed = true;
        } else if (path.startsWith("/manager/") && "MANAGER".equals(role)) {
            allowed = true;
        } else if (path.startsWith("/employee/") && "EMPLOYEE".equals(role)) {
            allowed = true;
        } else if (path.startsWith("/shared/")) {
            allowed = true;
        }

        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(req, res);
    }
}
