package com.taskvoice.service;

import com.taskvoice.ai.*;
import com.taskvoice.dao.*;
import com.taskvoice.dao.impl.*;
import com.taskvoice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AppraisalService {

    private static final Logger log = LoggerFactory.getLogger(AppraisalService.class);

    private final AppraisalDAO  appraisalDAO  = new AppraisalDAOImpl();
    private final TaskDAO       taskDAO       = new TaskDAOImpl();
    private final TimesheetDAO  timesheetDAO  = new TimesheetDAOImpl();
    private final UserDAO       userDAO       = new UserDAOImpl();
    private final AuditLogDAO   auditDAO      = new AuditLogDAOImpl();
    private final GeminiService geminiService = new GeminiService();
    private final NotificationService notifService = new NotificationService();

    public AppraisalPeriod createPeriod(String title, String periodType, String startDate,
                                         String endDate, int createdBy,
                                         int actorId, String actorName, String actorIp) {
        AppraisalPeriod p = new AppraisalPeriod();
        p.setTitle(title); p.setPeriodType(periodType);
        p.setStartDate(java.time.LocalDate.parse(startDate));
        p.setEndDate(java.time.LocalDate.parse(endDate));
        p.setCreatedBy(createdBy); p.setStatus("OPEN");
        int id = appraisalDAO.insertPeriod(p);
        p.setId(id);
        auditDAO.log(actorId, actorName, "APPRAISAL_PERIOD_CREATED", "APPRAISAL_PERIOD", id,
                     "Period: " + title, actorIp);
        return p;
    }

    /**
     * Trigger AI appraisal analysis for one employee in a period.
     * Saves AI values. Sets finalStatus to PENDING_REVIEW.
     * Manager must then Accept/Modify/Reject.
     */
    public Appraisal triggerAiAnalysis(int periodId, int employeeId, int managerId,
                                        int actorId, String actorName, String actorIp)
            throws Exception {

        AppraisalPeriod period = appraisalDAO.findPeriodById(periodId)
            .orElseThrow(() -> new IllegalArgumentException("Period not found: " + periodId));
        User employee = userDAO.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Get or create appraisal record
        Appraisal appraisal = appraisalDAO.findByPeriodAndEmployee(periodId, employeeId)
            .orElseGet(() -> {
                Appraisal a = new Appraisal();
                a.setPeriodId(periodId); a.setEmployeeId(employeeId); a.setManagerId(managerId);
                int id = appraisalDAO.insert(a);
                a.setId(id);
                return a;
            });

        // Gather data for the period
        List<Task> tasks = taskDAO.findByAssigneeId(employeeId, 1, 1000);
        List<TaskUpdate> allUpdates = new java.util.ArrayList<>();
        for (Task t : tasks) allUpdates.addAll(new ProgressUpdateDAOImpl().findByTaskId(t.getId()));
        List<TimesheetEntry> timesheetEntries = timesheetDAO.findByUserAndRange(
            employeeId, period.getStartDate(), period.getEndDate());

        // Run AI analysis (validates before saving)
        appraisal = geminiService.generateAppraisalAnalysis(employee, period, tasks, allUpdates, timesheetEntries, appraisal);
        appraisalDAO.updateAiAnalysis(appraisal);

        auditDAO.log(actorId, actorName, "APPRAISAL_GENERATED", "APPRAISAL", appraisal.getId(),
                     "AI analysis for employee " + employeeId + " in period " + periodId, actorIp);
        return appraisal;
    }

    /**
     * Manager finalizes appraisal: Accept / Modify / Reject AI recommendation.
     * Stores both AI values and manager decision side by side — never overwrites AI values.
     */
    public void finalizeAppraisal(int appraisalId, double managerScore, String managerGrade,
                                   String decision, String remark,
                                   int actorId, String actorName, String actorIp) {
        Appraisal a = appraisalDAO.findById(appraisalId)
            .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        appraisalDAO.updateManagerDecision(appraisalId, managerScore, managerGrade, decision, remark);
        notifService.notifyAppraisalAvailable(a.getEmployeeId(), appraisalId);
        auditDAO.log(actorId, actorName, "APPRAISAL_FINALIZED", "APPRAISAL", appraisalId,
                     "Decision: " + decision + " | Grade: " + managerGrade + " | Score: " + managerScore, actorIp);
    }

    public List<AppraisalPeriod> getPeriodsForManager(int managerId) {
        return appraisalDAO.findPeriodsByManagerId(managerId);
    }

    public Optional<AppraisalPeriod> findPeriodById(int id) { return appraisalDAO.findPeriodById(id); }
    public List<Appraisal> findByPeriod(int periodId)       { return appraisalDAO.findByPeriodId(periodId); }
    public Optional<Appraisal> findById(int id)             { return appraisalDAO.findById(id); }
    public List<Appraisal> findByEmployee(int employeeId)   { return appraisalDAO.findByEmployeeId(employeeId); }
}
