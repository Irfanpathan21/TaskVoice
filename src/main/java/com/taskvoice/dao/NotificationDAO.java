package com.taskvoice.dao;

import com.taskvoice.model.Notification;
import java.util.List;

public interface NotificationDAO {
    List<Notification> findByUserId(int userId);
    int countUnreadByUserId(int userId);
    int insert(Notification n);
    void markRead(int id);
    void markAllRead(int userId);
    boolean hasTodayNotification(int userId, String type);
}
