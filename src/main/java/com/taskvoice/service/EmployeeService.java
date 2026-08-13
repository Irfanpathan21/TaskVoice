package com.taskvoice.service;

import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import com.taskvoice.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final UserDAO       userDAO    = new UserDAOImpl();
    private final DepartmentDAO deptDAO    = new DepartmentDAOImpl();
    private final AuditLogDAO   auditDAO   = new AuditLogDAOImpl();
    private final NotificationService notifService = new NotificationService();

    /** Create a new employee or manager account. Admin-only. */
    public User createUser(String name, String email, String roleName, int roleId,
                           Integer departmentId, Integer managerId, String joiningDateStr,
                           int actorId, String actorName, String actorIp) {

        // Validate unique email
        if (userDAO.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        // Default temp password
        String tempPassword = "TaskVoice@123!";
        String hash = PasswordUtil.hash(tempPassword);
        String empNo = userDAO.generateEmployeeNo();

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(hash);
        user.setRoleId(roleId);
        user.setRoleName(roleName);
        user.setDepartmentId(departmentId);
        user.setStatus("ACTIVE");
        user.setForcePwChange(true);
        user.setEmployeeNo(empNo);
        if (joiningDateStr != null && !joiningDateStr.isBlank()) {
            user.setJoiningDate(LocalDate.parse(joiningDateStr));
        } else {
            user.setJoiningDate(LocalDate.now());
        }

        int userId = userDAO.insert(user);
        user.setId(userId);

        if (managerId != null && "EMPLOYEE".equals(roleName)) {
            userDAO.assignManager(userId, managerId);
        }

        auditDAO.log(actorId, actorName, "USER_CREATED", "USER", userId,
                     "Created " + roleName + " account: " + email, actorIp);
        return user;
    }

    public void updateUser(int userId, String name, String email, int roleId,
                           Integer departmentId, String joiningDateStr,
                           int actorId, String actorName, String actorIp) {
        Optional<User> found = userDAO.findById(userId);
        if (found.isEmpty()) throw new IllegalArgumentException("User not found: " + userId);
        User user = found.get();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setRoleId(roleId);
        user.setDepartmentId(departmentId);
        if (joiningDateStr != null && !joiningDateStr.isBlank()) {
            user.setJoiningDate(LocalDate.parse(joiningDateStr));
        }
        userDAO.update(user);
        auditDAO.log(actorId, actorName, "USER_UPDATED", "USER", userId, "Updated user: " + email, actorIp);
    }

    public void disableUser(int userId, int actorId, String actorName, String actorIp) {
        userDAO.updateStatus(userId, "DISABLED");
        auditDAO.log(actorId, actorName, "USER_DISABLED", "USER", userId, "Disabled user " + userId, actorIp);
    }

    public void enableUser(int userId, int actorId, String actorName, String actorIp) {
        userDAO.updateStatus(userId, "ACTIVE");
        auditDAO.log(actorId, actorName, "USER_ENABLED", "USER", userId, "Enabled user " + userId, actorIp);
    }

    public void assignManager(int employeeId, int managerId, int actorId, String actorName, String actorIp) {
        userDAO.assignManager(employeeId, managerId);
        auditDAO.log(actorId, actorName, "MANAGER_REASSIGNED", "USER", employeeId,
                     "Assigned manager " + managerId + " to employee " + employeeId, actorIp);
    }

    public Optional<User> findById(int id)         { return userDAO.findById(id); }
    public List<User>     findAll(int p, int size)  { return userDAO.findAll(p, size); }
    public List<User>     findByRole(String role)   { return userDAO.findByRole(role); }
    public int            countAll()                { return userDAO.countAll(); }
    public int            countByRole(String role)  { return userDAO.countByRole(role); }
    public List<User>     findTeam(int managerId)   { return userDAO.findByManagerId(managerId); }
    public List<Department> findAllDepartments()    { return deptDAO.findAll(); }

    /** Verify that employee actually reports to this manager (data isolation). */
    public boolean isMyEmployee(int managerId, int employeeId) {
        Optional<Integer> mgr = userDAO.findManagerIdForEmployee(employeeId);
        return mgr.isPresent() && mgr.get() == managerId;
    }
}
