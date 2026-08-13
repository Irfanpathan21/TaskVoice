package com.taskvoice.dao.impl;

import com.taskvoice.dao.TaskDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAOImpl implements TaskDAO {

    private static final Logger log = LoggerFactory.getLogger(TaskDAOImpl.class);
    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private static final String SELECT_BASE =
        "SELECT t.*, p.title AS project_title, " +
        "  a.name AS assignee_name, a.employee_no AS assignee_no, " +
        "  cat.name AS category_name, gb.name AS graded_by_name " +
        "FROM tasks t " +
        "JOIN projects p ON p.id = t.project_id " +
        "JOIN users a ON a.id = t.assignee_id " +
        "LEFT JOIN categories cat ON cat.id = t.category_id " +
        "LEFT JOIN users gb ON gb.id = t.graded_by ";

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setProjectId(rs.getInt("project_id"));
        t.setProjectTitle(rs.getString("project_title"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setAssigneeId(rs.getInt("assignee_id"));
        t.setAssigneeName(rs.getString("assignee_name"));
        t.setAssigneeNo(rs.getString("assignee_no"));
        int catId = rs.getInt("category_id");
        t.setCategoryId(rs.wasNull() ? null : catId);
        t.setCategoryName(rs.getString("category_name"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setStartDate(rs.getDate("start_date").toLocalDate());
        t.setDueDate(rs.getDate("due_date").toLocalDate());
        t.setExpectedHours(rs.getDouble("expected_hours"));
        t.setActualHours(rs.getDouble("actual_hours"));
        t.setCompletionPct(rs.getInt("completion_pct"));
        t.setManagerGrade(rs.getString("manager_grade"));
        double score = rs.getDouble("manager_score");
        t.setManagerScore(rs.wasNull() ? null : score);
        t.setManagerRemark(rs.getString("manager_remark"));
        Timestamp ga = rs.getTimestamp("graded_at");
        if (ga != null) t.setGradedAt(ga.toLocalDateTime());
        int gb = rs.getInt("graded_by");
        t.setGradedBy(rs.wasNull() ? null : gb);
        t.setGradedByName(rs.getString("graded_by_name"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) t.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) t.setUpdatedAt(ua.toLocalDateTime());
        return t;
    }

    @Override
    public Optional<Task> findById(int id) {
        String sql = SELECT_BASE + "WHERE t.id = ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findByProjectId(int projectId) {
        String sql = SELECT_BASE + "WHERE t.project_id = ? ORDER BY t.due_date, t.priority DESC";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findByAssigneeId(int assigneeId, int page, int pageSize) {
        String sql = SELECT_BASE + "WHERE t.assignee_id = ? ORDER BY t.due_date LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assigneeId); ps.setInt(2, pageSize); ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findByManagerTeam(int managerId, int page, int pageSize) {
        String sql = SELECT_BASE +
            "JOIN manager_assignments ma ON ma.employee_id = t.assignee_id AND ma.manager_id = ? " +
            "ORDER BY t.due_date LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId); ps.setInt(2, pageSize); ps.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findOverdueByManagerTeam(int managerId) {
        String sql = SELECT_BASE +
            "JOIN manager_assignments ma ON ma.employee_id = t.assignee_id AND ma.manager_id = ? " +
            "WHERE t.due_date < CURDATE() AND t.status NOT IN ('COMPLETED','CANCELLED')";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Task> findDueSoonByAssignee(int assigneeId, int days) {
        String sql = SELECT_BASE +
            "WHERE t.assignee_id = ? AND t.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
            "AND t.status NOT IN ('COMPLETED','CANCELLED') ORDER BY t.due_date";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assigneeId); ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByAssigneeId(int assigneeId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM tasks WHERE assignee_id=?")) {
            ps.setInt(1, assigneeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByManagerTeam(int managerId) {
        String sql = "SELECT COUNT(*) FROM tasks t JOIN manager_assignments ma ON ma.employee_id=t.assignee_id WHERE ma.manager_id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countOverdueByManagerTeam(int managerId) {
        String sql = "SELECT COUNT(*) FROM tasks t JOIN manager_assignments ma ON ma.employee_id=t.assignee_id " +
                     "WHERE ma.manager_id=? AND t.due_date < CURDATE() AND t.status NOT IN ('COMPLETED','CANCELLED')";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countPendingGradesByManagerId(int managerId) {
        String sql = "SELECT COUNT(*) FROM tasks t JOIN manager_assignments ma ON ma.employee_id=t.assignee_id " +
                     "WHERE ma.manager_id=? AND t.status='COMPLETED' AND t.manager_grade IS NULL";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(Task task) {
        String sql = "INSERT INTO tasks (project_id, title, description, assignee_id, category_id, " +
                     "priority, status, start_date, due_date, expected_hours) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getProjectId()); ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription()); ps.setInt(4, task.getAssigneeId());
            if (task.getCategoryId() != null) ps.setInt(5, task.getCategoryId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, task.getPriority() != null ? task.getPriority() : "MEDIUM");
            ps.setString(7, task.getStatus() != null ? task.getStatus() : "NOT_STARTED");
            ps.setDate(8, Date.valueOf(task.getStartDate())); ps.setDate(9, Date.valueOf(task.getDueDate()));
            ps.setDouble(10, task.getExpectedHours());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Task task) {
        String sql = "UPDATE tasks SET title=?, description=?, category_id=?, priority=?, " +
                     "status=?, start_date=?, due_date=?, expected_hours=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getTitle()); ps.setString(2, task.getDescription());
            if (task.getCategoryId() != null) ps.setInt(3, task.getCategoryId()); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, task.getPriority()); ps.setString(5, task.getStatus());
            ps.setDate(6, Date.valueOf(task.getStartDate())); ps.setDate(7, Date.valueOf(task.getDueDate()));
            ps.setDouble(8, task.getExpectedHours()); ps.setInt(9, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateStatus(int id, String status) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tasks SET status=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateCompletionPct(int id, int pct) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tasks SET completion_pct=?, updated_at=NOW() WHERE id=?")) {
            ps.setInt(1, pct); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateActualHours(int id, double hours) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tasks SET actual_hours=?, updated_at=NOW() WHERE id=?")) {
            ps.setDouble(1, hours); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void grade(int id, String grade, double score, String remark, int gradedBy) {
        String sql = "UPDATE tasks SET manager_grade=?, manager_score=?, manager_remark=?, " +
                     "graded_at=NOW(), graded_by=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, grade); ps.setDouble(2, score); ps.setString(3, remark);
            ps.setInt(4, gradedBy); ps.setInt(5, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tasks SET status='CANCELLED', updated_at=NOW() WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
