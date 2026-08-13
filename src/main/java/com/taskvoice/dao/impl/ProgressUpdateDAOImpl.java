package com.taskvoice.dao.impl;

import com.taskvoice.dao.ProgressUpdateDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.TaskUpdate;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressUpdateDAOImpl implements ProgressUpdateDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private TaskUpdate mapRow(ResultSet rs) throws SQLException {
        TaskUpdate u = new TaskUpdate();
        u.setId(rs.getInt("id"));
        u.setTaskId(rs.getInt("task_id"));
        u.setUserId(rs.getInt("user_id"));
        u.setUserName(rs.getString("user_name"));
        u.setUpdateSeq(rs.getInt("update_seq"));
        u.setRawText(rs.getString("raw_text"));
        u.setAiRephrasedText(rs.getString("ai_rephrased_text"));
        int pct = rs.getInt("completion_pct");
        u.setCompletionPct(rs.wasNull() ? null : pct);
        u.setProblemsFaced(rs.getString("problems_faced"));
        u.setNote(rs.getString("note"));
        int vr = rs.getInt("voice_record_id");
        u.setVoiceRecordId(rs.wasNull() ? null : vr);
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        return u;
    }

    @Override
    public List<TaskUpdate> findByTaskId(int taskId) {
        String sql = "SELECT tu.*, u.name AS user_name FROM task_updates tu " +
                     "JOIN users u ON u.id = tu.user_id WHERE tu.task_id = ? ORDER BY tu.update_seq";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                List<TaskUpdate> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByTaskId(int taskId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM task_updates WHERE task_id=?")) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int nextSeqForTask(int taskId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(update_seq), 0) + 1 FROM task_updates WHERE task_id=?")) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(TaskUpdate upd) {
        String sql = "INSERT INTO task_updates (task_id, user_id, update_seq, raw_text, " +
                     "ai_rephrased_text, completion_pct, problems_faced, note, voice_record_id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, upd.getTaskId()); ps.setInt(2, upd.getUserId());
            ps.setInt(3, upd.getUpdateSeq()); ps.setString(4, upd.getRawText());
            ps.setString(5, upd.getAiRephrasedText());
            if (upd.getCompletionPct() != null) ps.setInt(6, upd.getCompletionPct()); else ps.setNull(6, Types.INTEGER);
            ps.setString(7, upd.getProblemsFaced()); ps.setString(8, upd.getNote());
            if (upd.getVoiceRecordId() != null) ps.setInt(9, upd.getVoiceRecordId()); else ps.setNull(9, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
