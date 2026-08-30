package com.taskvoice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String employeeNo;
    private String name;
    private String email;
    private String passwordHash;
    private int roleId;
    private String roleName;
    private Integer departmentId;
    private String departmentName;
    private LocalDate joiningDate;
    private String status;
    private boolean forcePwChange;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Manager info (populated on join)
    private Integer managerId;
    private String managerName;

    public User() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isForcePwChange() { return forcePwChange; }
    public void setForcePwChange(boolean forcePwChange) { this.forcePwChange = forcePwChange; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public boolean isAdmin()    { return "ADMIN".equals(roleName); }
    public boolean isManager()  { return "MANAGER".equals(roleName); }
    public boolean isEmployee() { return "EMPLOYEE".equals(roleName); }
    public boolean isActive()   { return "ACTIVE".equals(status); }

    public String getAvatarLetter() {
        if (name != null && !name.isBlank()) {
            return name.trim().substring(0, 1).toUpperCase();
        }
        return "U";
    }
}
