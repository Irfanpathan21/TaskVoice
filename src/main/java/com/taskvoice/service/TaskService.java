package com.taskvoice.service;

import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskDAO             taskDAO       = new TaskDAOImpl();
    private final ProgressUpdateDAO   updateDAO     = new ProgressUpdateDAOImpl();
    private final TimesheetDAO        timesheetDAO  = new TimesheetDAOImpl();
    private final AuditLogDAO         auditDAO      = new AuditLogDAOImpl();
    private final NotificationService notifService  = new NotificationService();

    public Task createTask(int projectId, String title, String description,
                            int assigneeId, Integer categoryId, String priority,
                            LocalDate startDate, LocalDate dueDate, double expectedHours,
                            int actorId, String actorName, String actorIp) {

        if (dueDate.isBefore(startDate)) throw new IllegalArgumentException("Due date must be after start date.");

        Task t = new Task();
        t.setProjectId(projectId); t.setTitle(title.trim()); t.setDescription(description);
        t.setAssigneeId(assigneeId); t.setCategoryId(categoryId);
        t.setPriority(priority != null ? priority : "MEDIUM");
        t.setStatus("NOT_STARTED"); t.setStartDate(startDate); t.setDueDate(dueDate);
        t.setExpectedHours(expectedHours);
        int id = taskDAO.insert(t);
        t.setId(id);

        notifService.notifyTaskAssigned(assigneeId, id, title);
        auditDAO.log(actorId, actorName, "TASK_ASSIGNED", "TASK", id,
                     "Assigned task '" + title + "' to user " + assigneeId, actorIp);
        return t;
    }

    public void updateTask(Task task, int actorId, String actorName, String actorIp) {
        taskDAO.update(task);
        auditDAO.log(actorId, actorName, "TASK_UPDATED", "TASK", task.getId(), "Updated task: " + task.getTitle(), actorIp);
    }

    public void updateStatus(int taskId, String status, int actorId, String actorName, String actorIp) {
        // Validate transition
        Task task = findByIdOrThrow(taskId);
        if ("CANCELLED".equals(status)) {
            // Only manager (graded_by role) can cancel — enforced at servlet level via role
        }
        taskDAO.updateStatus(taskId, status);
        auditDAO.log(actorId, actorName, "TASK_STATUS_CHANGED", "TASK", taskId, "Status: " + status, actorIp);
    }

    /**
     * Grade a task. Manager-only. Can revise before task is folded into an appraisal.
     */
    public void gradeTask(int taskId, String grade, double score, String remark,
                           int managerId, String managerName, String managerIp) {
        taskDAO.grade(taskId, grade, score, remark, managerId);
        Task task = findByIdOrThrow(taskId);
        notifService.notifyFeedbackReceived(task.getAssigneeId(), taskId, task.getTitle());
        auditDAO.log(managerId, managerName, "MANAGER_GRADE_CHANGED", "TASK", taskId,
                     "Grade: " + grade + " (" + score + ")", managerIp);
    }

    /**
     * Submit a progress update (append-only).
     */
    public TaskUpdate addProgressUpdate(int taskId, int userId, String rawText,
                                         Integer completionPct, String problemsFaced,
                                         String note, Integer voiceRecordId,
                                         String aiRephrasedText) {
        int seq = updateDAO.nextSeqForTask(taskId);
        TaskUpdate u = new TaskUpdate();
        u.setTaskId(taskId); u.setUserId(userId); u.setUpdateSeq(seq);
        u.setRawText(rawText); u.setAiRephrasedText(aiRephrasedText);
        u.setCompletionPct(completionPct); u.setProblemsFaced(problemsFaced);
        u.setNote(note); u.setVoiceRecordId(voiceRecordId);
        int id = updateDAO.insert(u);
        u.setId(id);

        // Update task completion_pct if provided
        if (completionPct != null) {
            taskDAO.updateCompletionPct(taskId, completionPct);
            if (completionPct == 100) {
                taskDAO.updateStatus(taskId, "UNDER_REVIEW");
            }
        }

        // Rollup actual hours from timesheet
        double hours = timesheetDAO.sumHoursByTaskId(taskId);
        taskDAO.updateActualHours(taskId, hours);

        return u;
    }

    public List<TaskUpdate> getUpdates(int taskId) { return updateDAO.findByTaskId(taskId); }

    public Optional<Task> findById(int id) { return taskDAO.findById(id); }
    public List<Task>     findByProject(int projectId) { return taskDAO.findByProjectId(projectId); }
    public List<Task>     findByAssignee(int uid, int p, int s) { return taskDAO.findByAssigneeId(uid, p, s); }
    public List<Task>     findByManagerTeam(int mgId, int p, int s) { return taskDAO.findByManagerTeam(mgId, p, s); }
    public List<Task>     findDueSoon(int uid, int days) { return taskDAO.findDueSoonByAssignee(uid, days); }
    public List<Task>     findOverdue(int mgId) { return taskDAO.findOverdueByManagerTeam(mgId); }
    public int            countByAssignee(int uid) { return taskDAO.countByAssigneeId(uid); }
    public int            countByManagerTeam(int mgId) { return taskDAO.countByManagerTeam(mgId); }
    public int            countOverdue(int mgId) { return taskDAO.countOverdueByManagerTeam(mgId); }
    public int            countPendingGrades(int mgId) { return taskDAO.countPendingGradesByManagerId(mgId); }

    /** Verify employee owns this task (data isolation). */
    public boolean isMyTask(int taskId, int userId) {
        return findById(taskId).map(t -> t.getAssigneeId() == userId).orElse(false);
    }

    private Task findByIdOrThrow(int id) {
        return taskDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }
}
