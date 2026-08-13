package com.taskvoice.dao.impl;

import com.taskvoice.dao.AuditLogDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.AuditLog;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAOImpl implements AuditLogDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    @Override
    public void log(int actorId, String actorName, String action, String entityType,
                    Integer entityId, String detail, String ipAddress) {
        String sql = "INSERT INTO audit_logs (actor_id, actor_name, action, entity_type, entity_id, detail, ip_address) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actorId); ps.setString(2, actorName); ps.setString(3, action);
            ps.setString(4, entityType);
            if (entityId != null) ps.setInt(5, entityId); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, detail); ps.setString(7, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<AuditLog> findAll(int page, int pageSize, String actionFilter) {
        String sql = "SELECT * FROM audit_logs" +
            (actionFilter != null && !actionFilter.isBlank() ? " WHERE action=?" : "") +
            " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (actionFilter != null && !actionFilter.isBlank()) ps.setString(idx++, actionFilter);
            ps.setInt(idx++, pageSize); ps.setInt(idx, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<AuditLog> list = new ArrayList<>();
                while (rs.next()) {
                    AuditLog a = new AuditLog();
                    a.setId(rs.getLong("id")); a.setActorId(rs.getInt("actor_id"));
                    a.setActorName(rs.getString("actor_name")); a.setAction(rs.getString("action"));
                    a.setEntityType(rs.getString("entity_type"));
                    int eid = rs.getInt("entity_id"); a.setEntityId(rs.wasNull() ? null : eid);
                    a.setDetail(rs.getString("detail")); a.setIpAddress(rs.getString("ip_address"));
                    Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
                    list.add(a);
                }
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countAll(String actionFilter) {
        String sql = "SELECT COUNT(*) FROM audit_logs" +
            (actionFilter != null && !actionFilter.isBlank() ? " WHERE action=?" : "");
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (actionFilter != null && !actionFilter.isBlank()) ps.setString(1, actionFilter);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
