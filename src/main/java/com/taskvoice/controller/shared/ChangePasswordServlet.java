package com.taskvoice.controller.shared;

import com.taskvoice.model.User;
import com.taskvoice.service.AuthenticationService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/shared/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/shared/change-password.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getUser(session);

        String currentPw = req.getParameter("currentPassword");
        String newPw     = req.getParameter("newPassword");
        String confirmPw = req.getParameter("confirmPassword");

        if (newPw == null || !newPw.equals(confirmPw)) {
            req.setAttribute("error", "New passwords do not match.");
            req.getRequestDispatcher("/WEB-INF/views/shared/change-password.jsp").forward(req, resp);
            return;
        }
        if (newPw.length() < 8) {
            req.setAttribute("error", "Password must be at least 8 characters.");
            req.getRequestDispatcher("/WEB-INF/views/shared/change-password.jsp").forward(req, resp);
            return;
        }

        boolean success = authService.changePassword(user.getId(), currentPw, newPw);
        if (!success) {
            req.setAttribute("error", "Current password is incorrect.");
            req.getRequestDispatcher("/WEB-INF/views/shared/change-password.jsp").forward(req, resp);
            return;
        }

        // Refresh user in session with forcePwChange = false
        user.setForcePwChange(false);
        SessionUtil.setUser(session, user);
        resp.sendRedirect(req.getContextPath() + "/" + user.getRoleName().toLowerCase() + "/dashboard");
    }
}
