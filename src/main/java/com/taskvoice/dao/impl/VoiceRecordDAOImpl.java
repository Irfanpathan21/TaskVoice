package com.taskvoice.dao.impl;

import com.taskvoice.dao.VoiceRecordDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.VoiceRecord;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VoiceRecordDAOImpl implements VoiceRecordDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private VoiceRecord mapRow(ResultSet rs) throws SQLException {
        VoiceRecord v = new VoiceRecord();
        v.setId(rs.getInt("id"));
        v.setUserId(rs.getInt("user_id"));
        v.setAudioFileRef(rs.getString("audio_file_ref"));
        v.setTranscript(rs.getString("transcript"));
        v.setAiParsedJson(rs.getString("ai_parsed_json"));
        v.setProcessingStatus(rs.getString("processing_status"));
        v.setErrorMessage(rs.getString("error_message"));
        v.setRetryCount(rs.getInt("retry_count"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) v.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at"); if (ua != null) v.setUpdatedAt(ua.toLocalDateTime());
        return v;
    }

    @Override
    public Optional<VoiceRecord> findById(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM voice_records WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<VoiceRecord> findDraftsByUserId(int userId) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM voice_records WHERE user_id=? AND processing_status IN ('DRAFT','FAILED') ORDER BY created_at DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<VoiceRecord> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(VoiceRecord record) {
        String sql = "INSERT INTO voice_records (user_id, audio_file_ref, transcript, processing_status) VALUES (?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, record.getUserId()); ps.setString(2, record.getAudioFileRef());
            ps.setString(3, record.getTranscript());
            ps.setString(4, record.getProcessingStatus() != null ? record.getProcessingStatus() : "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateTranscript(int id, String transcript) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE voice_records SET transcript=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, transcript); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateParsedJson(int id, String json, String status) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE voice_records SET ai_parsed_json=?, processing_status=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, json); ps.setString(2, status); ps.setInt(3, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateStatus(int id, String status, String errorMessage) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE voice_records SET processing_status=?, error_message=?, updated_at=NOW() WHERE id=?")) {
            ps.setString(1, status); ps.setString(2, errorMessage); ps.setInt(3, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void incrementRetryCount(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE voice_records SET retry_count=retry_count+1, updated_at=NOW() WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
