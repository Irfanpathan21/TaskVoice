package com.taskvoice.dao.impl;

import com.taskvoice.dao.CategoryDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAOImpl implements CategoryDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id")); c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description")); c.setDefault(rs.getBoolean("is_default"));
        int cb = rs.getInt("created_by"); c.setCreatedBy(rs.wasNull() ? null : cb);
        try { c.setCreatedByName(rs.getString("creator_name")); } catch (SQLException ignored) {}
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        return c;
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT cat.*, u.name AS creator_name FROM categories cat LEFT JOIN users u ON u.id = cat.created_by ORDER BY cat.is_default DESC, cat.name";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Category> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Category> findById(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT cat.*, u.name AS creator_name FROM categories cat LEFT JOIN users u ON u.id=cat.created_by WHERE cat.id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(Category cat) {
        String sql = "INSERT INTO categories (name, description, is_default, created_by) VALUES (?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getName()); ps.setString(2, cat.getDescription());
            ps.setBoolean(3, cat.isDefault());
            if (cat.getCreatedBy() != null) ps.setInt(4, cat.getCreatedBy()); else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Category cat) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?")) {
            ps.setString(1, cat.getName()); ps.setString(2, cat.getDescription()); ps.setInt(3, cat.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int id) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM categories WHERE id=? AND is_default=FALSE")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
