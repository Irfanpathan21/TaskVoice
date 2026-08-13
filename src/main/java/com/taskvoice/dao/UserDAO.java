package com.taskvoice.dao;

import com.taskvoice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(int id);
    Optional<User> findByEmail(String email);
    List<User> findAll(int page, int pageSize);
    List<User> findByRole(String roleName);
    List<User> findByManagerId(int managerId);
    List<User> findByDepartmentId(int departmentId);
    int countAll();
    int countByRole(String roleName);
    int insert(User user);
    void update(User user);
    void updateStatus(int id, String status);
    void updatePassword(int id, String passwordHash, boolean forcePwChange);
    void assignManager(int employeeId, int managerId);
    void removeManagerAssignment(int employeeId);
    Optional<Integer> findManagerIdForEmployee(int employeeId);
    String generateEmployeeNo();
}
