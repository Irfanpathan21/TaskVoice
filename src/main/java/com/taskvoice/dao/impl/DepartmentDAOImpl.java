package com.taskvoice.dao.impl;

import com.taskvoice.dao.DepartmentDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAOImpl implements DepartmentDAO {

    private static final Logger log = LoggerFactory.getLogger(DepartmentDAOImpl.class);
    private DataSource ds() { return DBPoolListener.getDataSource(); }

    private Department mapRow(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        d.setDescription(rs.getString("description"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) d.setCreatedAt(ca.toLocalDateTime());
        try {
            d.setHeadcount(rs.getInt("headcount"));
        } catch (SQLException ignored) {}
        return d;
    }

    @Override
    public Optional<Department> findById(int id) {
        String sql = "SELECT d.*, COUNT(u.id) AS headcount FROM departments d " +
                     "LEFT JOIN users u ON u.department_id = d.id WHERE d.id = ? GROUP BY d.id";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Department> findAll() {
        String sql = "SELECT d.*, COUNT(u.id) AS headcount FROM departments d " +
                     "LEFT JOIN users u ON u.department_id = d.id GROUP BY d.id ORDER BY d.name";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Department> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int insert(Department dept) {
        String sql = "INSERT INTO departments (name, description) VALUES (?, ?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Department dept) {
        String sql = "UPDATE departments SET name=?, description=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getDescription());
            ps.setInt(3, dept.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM departments WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countEmployees(int departmentId) {
        String sql = "SELECT COUNT(*) FROM users WHERE department_id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
