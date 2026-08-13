package com.taskvoice.dao;

import com.taskvoice.model.Project;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectDAO {
    Optional<Project> findById(int id);
    List<Project> findByManagerId(int managerId, int page, int pageSize);
    List<Project> findByEmployeeId(int employeeId, int page, int pageSize);
    List<Project> findAll(int page, int pageSize);
    int countByManagerId(int managerId);
    int countByEmployeeId(int employeeId);
    int countAll();
    int insert(Project project);
    void update(Project project);
    void updateStatus(int id, String status);
    void updateAiSentiment(int id, String sentiment, double confidence, String explanation);
    void updateAiSummary(int id, String summary);
    void addMember(int projectId, int userId);
    void removeMember(int projectId, int userId);
    List<Integer> findMemberIds(int projectId);
    boolean isMember(int projectId, int userId);
    // Stats queries
    int countActiveByManagerId(int managerId);
    int countCompletedByManagerId(int managerId);
}
