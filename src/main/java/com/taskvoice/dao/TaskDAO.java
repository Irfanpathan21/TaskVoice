package com.taskvoice.dao;

import com.taskvoice.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    Optional<Task> findById(int id);
    List<Task> findByProjectId(int projectId);
    List<Task> findByAssigneeId(int assigneeId, int page, int pageSize);
    List<Task> findByManagerTeam(int managerId, int page, int pageSize);
    List<Task> findOverdueByManagerTeam(int managerId);
    List<Task> findDueSoonByAssignee(int assigneeId, int days);
    int countByAssigneeId(int assigneeId);
    int countByManagerTeam(int managerId);
    int countOverdueByManagerTeam(int managerId);
    int countPendingGradesByManagerId(int managerId);
    int insert(Task task);
    void update(Task task);
    void updateStatus(int id, String status);
    void updateCompletionPct(int id, int pct);
    void updateActualHours(int id, double hours);
    void grade(int id, String grade, double score, String remark, int gradedBy);
    void delete(int id);
}
