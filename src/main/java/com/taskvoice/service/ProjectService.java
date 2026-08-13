package com.taskvoice.service;

import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectDAO      projectDAO = new ProjectDAOImpl();
    private final AuditLogDAO     auditDAO   = new AuditLogDAOImpl();
    private final NotificationService notifService = new NotificationService();

    public Project createProject(String title, String description, int managerId,
                                  LocalDate startDate, LocalDate endDate,
                                  List<Integer> memberIds,
                                  int actorId, String actorName, String actorIp) {
        if (endDate.isBefore(startDate)) throw new IllegalArgumentException("End date must be after start date.");

        Project p = new Project();
        p.setTitle(title.trim()); p.setDescription(description);
        p.setManagerId(managerId); p.setStatus("PLANNING");
        p.setStartDate(startDate); p.setEndDate(endDate);
        int id = projectDAO.insert(p);
        p.setId(id);

        for (int memberId : memberIds) {
            projectDAO.addMember(id, memberId);
            notifService.notifyProjectAssigned(memberId, id, title);
        }

        auditDAO.log(actorId, actorName, "PROJECT_CREATED", "PROJECT", id,
                     "Created project: " + title, actorIp);
        return p;
    }

    public void updateProject(int id, String title, String description, String status,
                               LocalDate startDate, LocalDate endDate,
                               int actorId, String actorName, String actorIp) {
        Project p = findByIdOrThrow(id);
        p.setTitle(title.trim()); p.setDescription(description);
        p.setStatus(status); p.setStartDate(startDate); p.setEndDate(endDate);
        projectDAO.update(p);
    }

    public void addMember(int projectId, int userId, int actorId, String actorName, String actorIp) {
        verifyManagerOwns(projectId, actorId);
        projectDAO.addMember(projectId, userId);
        Project p = findByIdOrThrow(projectId);
        notifService.notifyProjectAssigned(userId, projectId, p.getTitle());
    }

    public void removeMember(int projectId, int userId, int actorId, String actorName) {
        verifyManagerOwns(projectId, actorId);
        projectDAO.removeMember(projectId, userId);
    }

    /**
     * Mark project completed. Triggers AI sentiment (handled by GeminiService separately
     * to keep async — this just updates status).
     */
    public void completeProject(int id, int actorId, String actorName, String actorIp) {
        verifyManagerOwns(id, actorId);
        Project p = findByIdOrThrow(id);
        projectDAO.updateStatus(id, "COMPLETED");
        notifService.notifyProjectCompleted(actorId, id, p.getTitle());
        auditDAO.log(actorId, actorName, "PROJECT_COMPLETED", "PROJECT", id, "Completed project: " + p.getTitle(), actorIp);
    }

    public Optional<Project> findById(int id)                     { return projectDAO.findById(id); }
    public List<Project>     findByManager(int mgId, int p, int s){ return projectDAO.findByManagerId(mgId, p, s); }
    public List<Project>     findByEmployee(int eId, int p, int s){ return projectDAO.findByEmployeeId(eId, p, s); }
    public int               countByManager(int mgId)             { return projectDAO.countByManagerId(mgId); }
    public int               countByEmployee(int eId)             { return projectDAO.countByEmployeeId(eId); }

    public void updateAiSentiment(int id, String sentiment, double confidence, String explanation) {
        projectDAO.updateAiSentiment(id, sentiment, confidence, explanation);
    }

    public void updateAiSummary(int id, String summary) {
        projectDAO.updateAiSummary(id, summary);
    }

    private Project findByIdOrThrow(int id) {
        return projectDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
    }

    private void verifyManagerOwns(int projectId, int managerId) {
        Project p = findByIdOrThrow(projectId);
        if (p.getManagerId() != managerId) {
            throw new SecurityException("Access denied: not your project.");
        }
    }
}
