package com.taskvoice.dao.impl;

import com.taskvoice.dao.ProjectDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDAOImpl implements ProjectDAO {

    private static final Logger log = LoggerFactory.getLogger(ProjectDAOImpl.class);
    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private static final String SELECT_BASE =
        "SELECT p.*, u.name AS manager_name, " +
        "  COUNT(DISTINCT t.id) AS total_tasks, " +
        "  COUNT(DISTINCT CASE WHEN t.status='COMPLETED' THEN t.id END) AS completed_tasks, " +
        "  COUNT(DISTINCT pm.user_id) AS member_count, " +
        "  COALESCE(AVG(te.duration_hours), 0) AS total_hours_sum " +
        "FROM projects p " +
        "JOIN users u ON u.id = p.manager_id " +
        "LEFT JOIN tasks t ON t.project_id = p.id " +
        "LEFT JOIN project_members pm ON pm.project_id = p.id " +
        "LEFT JOIN timesheet_entries te ON te.project_id = p.id ";

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setManagerId(rs.getInt("manager_id"));
        p.setManagerName(rs.getString("manager_name"));
        p.setStatus(rs.getString("status"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        p.setEndDate(rs.getDate("end_date").toLocalDate());
        p.setAiSummary(rs.getString("ai_summary"));
        p.setAiSentiment(rs.getString("ai_sentiment"));
        double conf = rs.getDouble("ai_sentiment_confidence");
        p.setAiSentimentConfidence(rs.wasNull() ? null : conf);
        p.setAiSentimentExplanation(rs.getString("ai_sentiment_explanation"));
        Timestamp sga = rs.getTimestamp("sentiment_generated_at");
        if (sga != null) p.setSentimentGeneratedAt(sga.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) p.setUpdatedAt(ua.toLocalDateTime());
        p.setTotalTasks(rs.getInt("total_tasks"));
        p.setCompletedTasks(rs.getInt("completed_tasks"));
        p.setMemberCount(rs.getInt("member_count"));
        int total = p.getTotalTasks();
        int done  = p.getCompletedTasks();
        p.setProgressPct(total > 0 ? (done * 100.0 / total) : 0);
        return p;
    }

    @Override
    public Optional<Project> findById(int id) {
        String sql = SELECT_BASE + "WHERE p.id = ? GROUP BY p.id";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Project> findByManagerId(int managerId, int page, int pageSize) {
        String sql = SELECT_BASE + "WHERE p.manager_id = ? GROUP BY p.id ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId); ps.setInt(2, pageSize); ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Project> findByEmployeeId(int employeeId, int page, int pageSize) {
        String sql = SELECT_BASE +
            "JOIN project_members pm2 ON pm2.project_id = p.id AND pm2.user_id = ? " +
            "GROUP BY p.id ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId); ps.setInt(2, pageSize); ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Project> findAll(int page, int pageSize) {
        String sql = SELECT_BASE + "GROUP BY p.id ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pageSize); ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByManagerId(int managerId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM projects WHERE manager_id=?")) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByEmployeeId(int employeeId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM project_members WHERE user_id=?")) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countAll() {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM projects");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(Project p) {
        String sql = "INSERT INTO projects (title, description, manager_id, status, start_date, end_date) VALUES (?,?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle()); ps.setString(2, p.getDescription());
            ps.setInt(3, p.getManagerId()); ps.setString(4, p.getStatus() != null ? p.getStatus() : "PLANNING");
            ps.setDate(5, Date.valueOf(p.getStartDate())); ps.setDate(6, Date.valueOf(p.getEndDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Project p) {
        String sql = "UPDATE projects SET title=?, description=?, status=?, start_date=?, end_date=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getTitle()); ps.setString(2, p.getDescription());
            ps.setString(3, p.getStatus()); ps.setDate(4, Date.valueOf(p.getStartDate()));
            ps.setDate(5, Date.valueOf(p.getEndDate())); ps.setInt(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateStatus(int id, String status) {
        String sql = "UPDATE projects SET status=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateAiSentiment(int id, String sentiment, double confidence, String explanation) {
        String sql = "UPDATE projects SET ai_sentiment=?, ai_sentiment_confidence=?, " +
                     "ai_sentiment_explanation=?, sentiment_generated_at=NOW(), updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sentiment); ps.setDouble(2, confidence);
            ps.setString(3, explanation); ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateAiSummary(int id, String summary) {
        String sql = "UPDATE projects SET ai_summary=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, summary); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void addMember(int projectId, int userId) {
        String sql = "INSERT IGNORE INTO project_members (project_id, user_id) VALUES (?,?)";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId); ps.setInt(2, userId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void removeMember(int projectId, int userId) {
        String sql = "DELETE FROM project_members WHERE project_id=? AND user_id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId); ps.setInt(2, userId); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Integer> findMemberIds(int projectId) {
        String sql = "SELECT user_id FROM project_members WHERE project_id=?";
        List<Integer> ids = new ArrayList<>();
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getInt(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return ids;
    }

    @Override
    public boolean isMember(int projectId, int userId) {
        String sql = "SELECT 1 FROM project_members WHERE project_id=? AND user_id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId); ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countActiveByManagerId(int managerId) {
        String sql = "SELECT COUNT(*) FROM projects WHERE manager_id=? AND status='ACTIVE'";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countCompletedByManagerId(int managerId) {
        String sql = "SELECT COUNT(*) FROM projects WHERE manager_id=? AND status='COMPLETED'";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
