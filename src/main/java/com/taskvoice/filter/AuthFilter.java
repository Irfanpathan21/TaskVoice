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
 * AuthFilter — ensures every request to protected paths has an authenticated session.
 * Applied to all paths except /login, /assets, /css, /js.
 */
@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/employee/*", "/shared/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        User user = SessionUtil.getUser(session);

        if (user == null) {
            String loginUrl = request.getContextPath() + "/login";
            response.sendRedirect(loginUrl);
            return;
        }

        // Force password change before any other page
        if (user.isForcePwChange()) {
            String changePasswordUrl = request.getContextPath() + "/shared/change-password";
            String requestedPath = request.getServletPath();
            if (!requestedPath.startsWith("/shared/change-password")) {
                response.sendRedirect(changePasswordUrl);
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
