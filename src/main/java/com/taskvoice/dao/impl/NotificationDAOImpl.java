package com.taskvoice.dao.impl;

import com.taskvoice.dao.NotificationDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Notification;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setLink(rs.getString("link"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) n.setCreatedAt(ca.toLocalDateTime());
        return n;
    }

    @Override
    public List<Notification> findByUserId(int userId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 50")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countUnreadByUserId(int userId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=FALSE")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(Notification n) {
        String sql = "INSERT INTO notifications (user_id, type, title, message, link) VALUES (?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId()); ps.setString(2, n.getType());
            ps.setString(3, n.getTitle()); ps.setString(4, n.getMessage());
            ps.setString(5, n.getLink());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void markRead(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void markAllRead(int userId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE user_id=?")) {
            ps.setInt(1, userId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean hasTodayNotification(int userId, String type) {
        String sql = "SELECT 1 FROM notifications WHERE user_id=? AND type=? AND DATE(created_at)=CURDATE() LIMIT 1";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
