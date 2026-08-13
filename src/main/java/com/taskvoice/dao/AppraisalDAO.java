package com.taskvoice.dao;

import com.taskvoice.model.Appraisal;
import com.taskvoice.model.AppraisalPeriod;
import java.util.List;
import java.util.Optional;

public interface AppraisalDAO {
    // Appraisal Periods
    int insertPeriod(AppraisalPeriod period);
    void updatePeriodStatus(int id, String status);
    Optional<AppraisalPeriod> findPeriodById(int id);
    List<AppraisalPeriod> findPeriodsByManagerId(int managerId);
    List<AppraisalPeriod> findAllPeriods();

    // Appraisals
    int insert(Appraisal appraisal);
    Optional<Appraisal> findById(int id);
    Optional<Appraisal> findByPeriodAndEmployee(int periodId, int employeeId);
    List<Appraisal> findByEmployeeId(int employeeId);
    List<Appraisal> findByPeriodId(int periodId);
    void updateAiAnalysis(Appraisal appraisal);
    void updateManagerDecision(int id, double managerScore, String managerGrade,
                               String decision, String remark);
}
