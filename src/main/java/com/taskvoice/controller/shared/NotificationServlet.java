package com.taskvoice.controller.shared;

import com.taskvoice.model.Notification;
import com.taskvoice.model.User;
import com.taskvoice.service.NotificationService;
import com.taskvoice.util.JsonUtil;
import com.taskvoice.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/shared/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationService service = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getUser(req.getSession(false));
        if (user == null) { resp.setStatus(401); return; }

        String action = req.getParameter("action");
        resp.setContentType("application/json");

        if ("count".equals(action)) {
            int count = service.countUnread(user.getId());
            resp.getWriter().write("{\"count\":" + count + "}");
        } else {
            List<Notification> notifs = service.getForUser(user.getId());
            resp.getWriter().write(JsonUtil.toJson(notifs));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getUser(req.getSession(false));
        if (user == null) { resp.setStatus(401); return; }

        String action = req.getParameter("action");
        resp.setContentType("application/json");

        if ("markRead".equals(action)) {
            String idStr = req.getParameter("id");
            if (idStr != null) service.markRead(Integer.parseInt(idStr));
            resp.getWriter().write(JsonUtil.ok("Marked as read"));
        } else if ("markAllRead".equals(action)) {
            service.markAllRead(user.getId());
            resp.getWriter().write(JsonUtil.ok("All marked as read"));
        }
    }
}
