package com.taskvoice.util;

import jakarta.servlet.http.HttpSession;
import com.taskvoice.model.User;

public final class SessionUtil {

    public static final String SESSION_USER = "currentUser";
    public static final String SESSION_CSRF = "csrfToken";
    private static final int SESSION_TIMEOUT_SECONDS = 1800; // 30 minutes

    private SessionUtil() {}

    public static void setUser(HttpSession session, User user) {
        session.setAttribute(SESSION_USER, user);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }

    public static User getUser(HttpSession session) {
        if (session == null) return null;
        return (User) session.getAttribute(SESSION_USER);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getUser(session) != null;
    }

    public static String getRole(HttpSession session) {
        User u = getUser(session);
        return u != null ? u.getRoleName() : null;
    }

    public static void setCsrfToken(HttpSession session, String token) {
        session.setAttribute(SESSION_CSRF, token);
    }

    public static String getCsrfToken(HttpSession session) {
        return session == null ? null : (String) session.getAttribute(SESSION_CSRF);
    }

    public static void invalidate(HttpSession session) {
        if (session != null) {
            try { session.invalidate(); } catch (Exception ignored) {}
        }
    }
}
