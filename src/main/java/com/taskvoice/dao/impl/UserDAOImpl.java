package com.taskvoice.dao.impl;

import com.taskvoice.dao.UserDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    private DataSource ds() {
        return DBPoolListener.getDataSource();
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmployeeNo(rs.getString("employee_no"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setStatus(rs.getString("status"));
        u.setForcePwChange(rs.getBoolean("force_pw_change"));

        int deptId = rs.getInt("department_id");
        u.setDepartmentId(rs.wasNull() ? null : deptId);
        u.setDepartmentName(rs.getString("department_name"));

        Date jd = rs.getDate("joining_date");
        if (jd != null)
            u.setJoiningDate(jd.toLocalDate());

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null)
            u.setCreatedAt(ca.toLocalDateTime());

        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null)
            u.setUpdatedAt(ua.toLocalDateTime());

        // Manager info if present in result set
        try {
            int mgId = rs.getInt("manager_id");
            if (!rs.wasNull()) {
                u.setManagerId(mgId);
                u.setManagerName(rs.getString("manager_name"));
            }
        } catch (SQLException ignored) {
        }

        return u;
    }

    private static final String SELECT_BASE = "SELECT u.*, r.name AS role_name, d.name AS department_name, " +
            "       ma.manager_id, m.name AS manager_name " +
            "FROM users u " +
            "JOIN roles r ON r.id = u.role_id " +
            "LEFT JOIN departments d ON d.id = u.department_id " +
            "LEFT JOIN manager_assignments ma ON ma.employee_id = u.id " +
            "LEFT JOIN users m ON m.id = ma.manager_id ";

    @Override
    public Optional<User> findById(int id) {
        String sql = SELECT_BASE + "WHERE u.id = ?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("findById failed for id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = SELECT_BASE + "WHERE u.email = ?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("findByEmail failed for email={}", email, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll(int page, int pageSize) {
        String sql = SELECT_BASE + "ORDER BY u.id LIMIT ? OFFSET ?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next())
                    list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            log.error("findAll failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findByRole(String roleName) {
        String sql = SELECT_BASE + "WHERE r.name = ? ORDER BY u.name";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next())
                    list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            log.error("findByRole failed for role={}", roleName, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findByManagerId(int managerId) {
        String sql = SELECT_BASE + "WHERE ma.manager_id = ? AND r.name = 'EMPLOYEE' ORDER BY u.name";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next())
                    list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            log.error("findByManagerId failed for managerId={}", managerId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findByDepartmentId(int departmentId) {
        String sql = SELECT_BASE + "WHERE u.department_id = ? ORDER BY u.name";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next())
                    list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException e) {
            log.error("findByDepartmentId failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countByRole(String roleName) {
        String sql = "SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.role_id WHERE r.name = ?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int insert(User user) {
        String sql = "INSERT INTO users (employee_no, name, email, password_hash, role_id, " +
                "department_id, joining_date, status, force_pw_change) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmployeeNo());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setInt(5, user.getRoleId());
            if (user.getDepartmentId() != null)
                ps.setInt(6, user.getDepartmentId());
            else
                ps.setNull(6, Types.INTEGER);
            if (user.getJoiningDate() != null)
                ps.setDate(7, Date.valueOf(user.getJoiningDate()));
            else
                ps.setNull(7, Types.DATE);
            ps.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            ps.setBoolean(9, user.isForcePwChange());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    return keys.getInt(1);
            }
        } catch (SQLException e) {
            log.error("insert user failed for email={}", user.getEmail(), e);
            throw new RuntimeException(e);
        }
        return -1;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET name=?, email=?, role_id=?, department_id=?, " +
                "joining_date=?, status=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRoleId());
            if (user.getDepartmentId() != null)
                ps.setInt(4, user.getDepartmentId());
            else
                ps.setNull(4, Types.INTEGER);
            if (user.getJoiningDate() != null)
                ps.setDate(5, Date.valueOf(user.getJoiningDate()));
            else
                ps.setNull(5, Types.DATE);
            ps.setString(6, user.getStatus());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("update user failed for id={}", user.getId(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatus(int id, String status) {
        String sql = "UPDATE users SET status=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(int id, String passwordHash, boolean forcePwChange) {
        String sql = "UPDATE users SET password_hash=?, force_pw_change=?, updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setBoolean(2, forcePwChange);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignManager(int employeeId, int managerId) {
        String sql = "INSERT INTO manager_assignments (employee_id, manager_id) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE manager_id=?, assigned_at=NOW()";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, managerId);
            ps.setInt(3, managerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeManagerAssignment(int employeeId) {
        String sql = "DELETE FROM manager_assignments WHERE employee_id=?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Integer> findManagerIdForEmployee(int employeeId) {
        String sql = "SELECT manager_id FROM manager_assignments WHERE employee_id=?";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return rs.wasNull() ? Optional.empty() : Optional.of(id);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateEmployeeNo() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = ds().getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            int count = rs.next() ? rs.getInt(1) : 0;
            return String.format("EMP-%03d", count + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
