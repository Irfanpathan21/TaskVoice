package com.taskvoice.dao.impl;

import com.taskvoice.dao.TimesheetDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.TimesheetEntry;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimesheetDAOImpl implements TimesheetDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private static final String SELECT_BASE =
        "SELECT te.*, u.name AS user_name, t.title AS task_title, " +
        "  p.title AS project_title, cat.name AS category_name " +
        "FROM timesheet_entries te " +
        "JOIN users u ON u.id = te.user_id " +
        "LEFT JOIN tasks t ON t.id = te.task_id " +
        "LEFT JOIN projects p ON p.id = te.project_id " +
        "LEFT JOIN categories cat ON cat.id = te.category_id ";

    private TimesheetEntry mapRow(ResultSet rs) throws SQLException {
        TimesheetEntry e = new TimesheetEntry();
        e.setId(rs.getInt("id"));
        e.setUserId(rs.getInt("user_id"));
        e.setUserName(rs.getString("user_name"));
        int vr = rs.getInt("voice_record_id"); e.setVoiceRecordId(rs.wasNull() ? null : vr);
        int tk = rs.getInt("task_id");         e.setTaskId(rs.wasNull() ? null : tk);
        e.setTaskTitle(rs.getString("task_title"));
        int pj = rs.getInt("project_id");      e.setProjectId(rs.wasNull() ? null : pj);
        e.setProjectTitle(rs.getString("project_title"));
        int ct = rs.getInt("category_id");     e.setCategoryId(rs.wasNull() ? null : ct);
        e.setCategoryName(rs.getString("category_name"));
        e.setEntryDate(rs.getDate("entry_date").toLocalDate());
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDurationHours(rs.getDouble("duration_hours"));
        e.setConfirmed(rs.getBoolean("is_confirmed"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) e.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at"); if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());
        return e;
    }

    @Override
    public Optional<TimesheetEntry> findById(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE te.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<TimesheetEntry> findByUserAndDate(int userId, LocalDate date) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE te.user_id=? AND te.entry_date=? ORDER BY te.created_at")) {
            ps.setInt(1, userId); ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<TimesheetEntry> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<TimesheetEntry> findByUserAndRange(int userId, LocalDate from, LocalDate to) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE te.user_id=? AND te.entry_date BETWEEN ? AND ? ORDER BY te.entry_date DESC, te.created_at DESC")) {
            ps.setInt(1, userId); ps.setDate(2, Date.valueOf(from)); ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                List<TimesheetEntry> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<TimesheetEntry> findByManagerTeamAndRange(int managerId, LocalDate from, LocalDate to) {
        String sql = SELECT_BASE +
            "JOIN manager_assignments ma ON ma.employee_id = te.user_id AND ma.manager_id=? " +
            "WHERE te.entry_date BETWEEN ? AND ? ORDER BY te.entry_date DESC, u.name";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId); ps.setDate(2, Date.valueOf(from)); ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                List<TimesheetEntry> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<TimesheetEntry> findByTaskId(int taskId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE te.task_id=? ORDER BY te.entry_date")) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TimesheetEntry> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double sumHoursByUserAndDate(int userId, LocalDate date) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(duration_hours),0) FROM timesheet_entries WHERE user_id=? AND entry_date=?")) {
            ps.setInt(1, userId); ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double sumHoursByTaskId(int taskId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(duration_hours),0) FROM timesheet_entries WHERE task_id=?")) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public double sumHoursByUserAndRange(int userId, LocalDate from, LocalDate to) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(duration_hours),0) FROM timesheet_entries WHERE user_id=? AND entry_date BETWEEN ? AND ?")) {
            ps.setInt(1, userId); ps.setDate(2, Date.valueOf(from)); ps.setDate(3, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean hasEntryForToday(int userId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM timesheet_entries WHERE user_id=? AND entry_date=CURDATE() LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(TimesheetEntry entry) {
        String sql = "INSERT INTO timesheet_entries (user_id, voice_record_id, task_id, project_id, category_id, " +
                     "entry_date, title, description, duration_hours, is_confirmed) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entry.getUserId());
            if (entry.getVoiceRecordId() != null) ps.setInt(2, entry.getVoiceRecordId()); else ps.setNull(2, Types.INTEGER);
            if (entry.getTaskId() != null)         ps.setInt(3, entry.getTaskId());        else ps.setNull(3, Types.INTEGER);
            if (entry.getProjectId() != null)      ps.setInt(4, entry.getProjectId());     else ps.setNull(4, Types.INTEGER);
            if (entry.getCategoryId() != null)     ps.setInt(5, entry.getCategoryId());    else ps.setNull(5, Types.INTEGER);
            ps.setDate(6, Date.valueOf(entry.getEntryDate()));
            ps.setString(7, entry.getTitle()); ps.setString(8, entry.getDescription());
            ps.setDouble(9, entry.getDurationHours()); ps.setBoolean(10, entry.isConfirmed());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(TimesheetEntry entry) {
        String sql = "UPDATE timesheet_entries SET task_id=?, project_id=?, category_id=?, " +
                     "title=?, description=?, duration_hours=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (entry.getTaskId() != null)     ps.setInt(1, entry.getTaskId());    else ps.setNull(1, Types.INTEGER);
            if (entry.getProjectId() != null)  ps.setInt(2, entry.getProjectId()); else ps.setNull(2, Types.INTEGER);
            if (entry.getCategoryId() != null) ps.setInt(3, entry.getCategoryId());else ps.setNull(3, Types.INTEGER);
            ps.setString(4, entry.getTitle()); ps.setString(5, entry.getDescription());
            ps.setDouble(6, entry.getDurationHours()); ps.setInt(7, entry.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void confirm(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE timesheet_entries SET is_confirmed=TRUE, updated_at=NOW() WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM timesheet_entries WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
