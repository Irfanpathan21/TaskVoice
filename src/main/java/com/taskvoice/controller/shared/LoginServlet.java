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
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Already logged in? Redirect to dashboard
        HttpSession session = req.getSession(false);
        User user = SessionUtil.getUser(session);
        if (user != null) {
            redirectToDashboard(resp, req, user);
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/shared/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Email and password are required.");
            req.getRequestDispatcher("/WEB-INF/views/shared/login.jsp").forward(req, resp);
            return;
        }

        Optional<User> result = authService.login(email, password, req.getRemoteAddr());

        if (result.isEmpty()) {
            req.setAttribute("error", "Invalid email or password.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/shared/login.jsp").forward(req, resp);
            return;
        }

        User user = result.get();
        HttpSession session = req.getSession(true);
        SessionUtil.setUser(session, user);
        redirectToDashboard(resp, req, user);
    }

    private void redirectToDashboard(HttpServletResponse resp, HttpServletRequest req, User user)
            throws IOException {
        String ctx = req.getContextPath();
        String dest = switch (user.getRoleName()) {
            case "ADMIN"    -> ctx + "/admin/dashboard";
            case "MANAGER"  -> ctx + "/manager/dashboard";
            case "EMPLOYEE" -> ctx + "/employee/dashboard";
            default         -> ctx + "/login";
        };
        resp.sendRedirect(dest);
    }
}
