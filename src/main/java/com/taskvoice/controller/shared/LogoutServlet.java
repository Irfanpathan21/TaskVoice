package com.taskvoice.controller.shared;

import com.taskvoice.model.User;
import com.taskvoice.service.AuthenticationService;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private final AuthenticationService authService = new AuthenticationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = SessionUtil.getUser(session);
            authService.logout(user, req.getRemoteAddr());
            SessionUtil.invalidate(session);
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
