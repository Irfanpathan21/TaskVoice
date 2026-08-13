package com.taskvoice.service;

import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationDAO dao = new NotificationDAOImpl();

    public void notifyTaskAssigned(int employeeId, int taskId, String taskTitle) {
        send(employeeId, "TASK_ASSIGNED", "New Task Assigned",
             "You have been assigned a new task: " + taskTitle,
             "/employee/tasks?id=" + taskId);
    }

    public void notifyProjectAssigned(int employeeId, int projectId, String projectTitle) {
        send(employeeId, "PROJECT_ASSIGNED", "Added to Project",
             "You have been added to project: " + projectTitle,
             "/employee/projects?id=" + projectId);
    }

    public void notifyFeedbackReceived(int employeeId, int taskId, String taskTitle) {
        send(employeeId, "FEEDBACK_RECEIVED", "Manager Feedback",
             "Your manager has graded task: " + taskTitle,
             "/employee/tasks?id=" + taskId);
    }

    public void notifyAppraisalAvailable(int employeeId, int appraisalId) {
        send(employeeId, "APPRAISAL_AVAILABLE", "Appraisal Available",
             "Your appraisal has been finalized. View your results.",
             "/employee/appraisal?id=" + appraisalId);
    }

    public void notifyProjectCompleted(int managerId, int projectId, String projectTitle) {
        send(managerId, "PROJECT_COMPLETED", "Project Completed",
             "Project '" + projectTitle + "' has been marked as completed.",
             "/manager/projects?id=" + projectId);
    }

    /** Send end-of-day nudge — fires at most once per day per user. */
    public void sendDailyTimesheetNudge(int userId) {
        if (!dao.hasTodayNotification(userId, "NO_TIMESHEET_TODAY")) {
            send(userId, "NO_TIMESHEET_TODAY", "No Timesheet Logged Today",
                 "Don't forget to log your work for today. Record your daily update.",
                 "/employee/voice-timesheet");
        }
    }

    private void send(int userId, String type, String title, String message, String link) {
        Notification n = new Notification();
        n.setUserId(userId); n.setType(type); n.setTitle(title);
        n.setMessage(message); n.setLink(link);
        try { dao.insert(n); } catch (Exception e) { log.error("Failed to send notification", e); }
    }

    public List<Notification> getForUser(int userId) { return dao.findByUserId(userId); }
    public int countUnread(int userId)               { return dao.countUnreadByUserId(userId); }
    public void markRead(int id)                     { dao.markRead(id); }
    public void markAllRead(int userId)              { dao.markAllRead(userId); }
}
