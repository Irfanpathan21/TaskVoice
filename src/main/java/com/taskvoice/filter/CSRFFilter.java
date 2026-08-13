package com.taskvoice.filter;

import com.taskvoice.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRFFilter — generates and validates CSRF tokens on all state-changing requests.
 * GET requests inject the token into the session.
 * POST/PUT/DELETE requests validate the token before any servlet logic runs.
 */
@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/employee/*", "/shared/*"})
public class CSRFFilter implements Filter {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        if (session == null) {
            chain.doFilter(req, res);
            return;
        }

        String method = request.getMethod().toUpperCase();

        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            // Ensure a CSRF token exists
            if (SessionUtil.getCsrfToken(session) == null) {
                SessionUtil.setCsrfToken(session, generateToken());
            }
            chain.doFilter(req, res);
            return;
        }

        // For state-changing methods, validate the token
        String sessionToken = SessionUtil.getCsrfToken(session);
        String requestToken = request.getParameter("_csrf");
        if (requestToken == null) {
            requestToken = request.getHeader("X-CSRF-Token");
        }

        if (sessionToken == null || !sessionToken.equals(requestToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token mismatch");
            return;
        }

        chain.doFilter(req, res);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
