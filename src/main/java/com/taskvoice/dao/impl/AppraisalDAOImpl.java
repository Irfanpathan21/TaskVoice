package com.taskvoice.dao.impl;

import com.taskvoice.dao.AppraisalDAO;
import com.taskvoice.listener.DBPoolListener;
import com.taskvoice.model.Appraisal;
import com.taskvoice.model.AppraisalPeriod;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppraisalDAOImpl implements AppraisalDAO {

    private DataSource ds() { return DBPoolListener.getDataSource(); }

    // ====== PERIODS ======

    @Override
    public int insertPeriod(AppraisalPeriod p) {
        String sql = "INSERT INTO appraisal_periods (title, period_type, start_date, end_date, created_by) VALUES (?,?,?,?,?)";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle()); ps.setString(2, p.getPeriodType());
            ps.setDate(3, Date.valueOf(p.getStartDate())); ps.setDate(4, Date.valueOf(p.getEndDate()));
            ps.setInt(5, p.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updatePeriodStatus(int id, String status) {
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE appraisal_periods SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<AppraisalPeriod> findPeriodById(int id) {
        String sql = "SELECT ap.*, u.name AS creator_name FROM appraisal_periods ap JOIN users u ON u.id=ap.created_by WHERE ap.id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapPeriod(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<AppraisalPeriod> findPeriodsByManagerId(int managerId) {
        String sql = "SELECT ap.*, u.name AS creator_name FROM appraisal_periods ap JOIN users u ON u.id=ap.created_by WHERE ap.created_by=? ORDER BY ap.created_at DESC";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<AppraisalPeriod> list = new ArrayList<>();
                while (rs.next()) list.add(mapPeriod(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<AppraisalPeriod> findAllPeriods() {
        String sql = "SELECT ap.*, u.name AS creator_name FROM appraisal_periods ap JOIN users u ON u.id=ap.created_by ORDER BY ap.created_at DESC";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AppraisalPeriod> list = new ArrayList<>();
            while (rs.next()) list.add(mapPeriod(rs));
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private AppraisalPeriod mapPeriod(ResultSet rs) throws SQLException {
        AppraisalPeriod p = new AppraisalPeriod();
        p.setId(rs.getInt("id")); p.setTitle(rs.getString("title"));
        p.setPeriodType(rs.getString("period_type"));
        p.setStartDate(rs.getDate("start_date").toLocalDate());
        p.setEndDate(rs.getDate("end_date").toLocalDate());
        p.setCreatedBy(rs.getInt("created_by")); p.setCreatedByName(rs.getString("creator_name"));
        p.setStatus(rs.getString("status"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) p.setCreatedAt(ca.toLocalDateTime());
        return p;
    }

    // ====== APPRAISALS ======

    @Override
    public int insert(Appraisal a) {
        String sql = "INSERT INTO appraisals (period_id, employee_id, manager_id, final_status) VALUES (?,?,?,'PENDING_AI')";
        try (Connection c = ds().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPeriodId()); ps.setInt(2, a.getEmployeeId()); ps.setInt(3, a.getManagerId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : -1; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Appraisal> findById(int id) {
        String sql = buildSelectBase() + "WHERE a.id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapAppraisal(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Appraisal> findByPeriodAndEmployee(int periodId, int employeeId) {
        String sql = buildSelectBase() + "WHERE a.period_id=? AND a.employee_id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId); ps.setInt(2, employeeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapAppraisal(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Appraisal> findByEmployeeId(int employeeId) {
        String sql = buildSelectBase() + "WHERE a.employee_id=? ORDER BY a.created_at DESC";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appraisal> list = new ArrayList<>();
                while (rs.next()) list.add(mapAppraisal(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Appraisal> findByPeriodId(int periodId) {
        String sql = buildSelectBase() + "WHERE a.period_id=? ORDER BY emp.name";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, periodId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Appraisal> list = new ArrayList<>();
                while (rs.next()) list.add(mapAppraisal(rs));
                return list;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateAiAnalysis(Appraisal a) {
        String sql = "UPDATE appraisals SET ai_score=?, ai_grade=?, ai_promotion_rec=?, ai_increment_range=?, " +
                     "ai_summary=?, ai_strengths=?, ai_improvements=?, ai_productivity_analysis=?, " +
                     "ai_reliability_analysis=?, ai_consistency_analysis=?, ai_problem_solving=?, " +
                     "ai_generated_at=NOW(), final_status='PENDING_REVIEW', updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setDoubleOrNull(ps, 1, a.getAiScore()); ps.setString(2, a.getAiGrade());
            ps.setString(3, a.getAiPromotionRec()); ps.setString(4, a.getAiIncrementRange());
            ps.setString(5, a.getAiSummary()); ps.setString(6, a.getAiStrengths());
            ps.setString(7, a.getAiImprovements()); ps.setString(8, a.getAiProductivityAnalysis());
            ps.setString(9, a.getAiReliabilityAnalysis()); ps.setString(10, a.getAiConsistencyAnalysis());
            ps.setString(11, a.getAiProblemSolving()); ps.setInt(12, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void updateManagerDecision(int id, double managerScore, String managerGrade,
                                       String decision, String remark) {
        String sql = "UPDATE appraisals SET manager_score=?, manager_grade=?, manager_decision=?, " +
                     "manager_remark=?, manager_reviewed_at=NOW(), final_status='FINALIZED', updated_at=NOW() WHERE id=?";
        try (Connection c = ds().getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, managerScore); ps.setString(2, managerGrade);
            ps.setString(3, decision); ps.setString(4, remark); ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private String buildSelectBase() {
        return "SELECT a.*, ap.title AS period_title, " +
               "emp.name AS employee_name, emp.employee_no AS employee_no, " +
               "mgr.name AS manager_name " +
               "FROM appraisals a " +
               "JOIN appraisal_periods ap ON ap.id = a.period_id " +
               "JOIN users emp ON emp.id = a.employee_id " +
               "JOIN users mgr ON mgr.id = a.manager_id ";
    }

    private Appraisal mapAppraisal(ResultSet rs) throws SQLException {
        Appraisal a = new Appraisal();
        a.setId(rs.getInt("id")); a.setPeriodId(rs.getInt("period_id")); a.setPeriodTitle(rs.getString("period_title"));
        a.setEmployeeId(rs.getInt("employee_id")); a.setEmployeeName(rs.getString("employee_name"));
        a.setEmployeeNo(rs.getString("employee_no"));
        a.setManagerId(rs.getInt("manager_id")); a.setManagerName(rs.getString("manager_name"));
        setNullableDouble(rs, "ai_score", a::setAiScore);
        a.setAiGrade(rs.getString("ai_grade")); a.setAiPromotionRec(rs.getString("ai_promotion_rec"));
        a.setAiIncrementRange(rs.getString("ai_increment_range")); a.setAiSummary(rs.getString("ai_summary"));
        a.setAiStrengths(rs.getString("ai_strengths")); a.setAiImprovements(rs.getString("ai_improvements"));
        a.setAiProductivityAnalysis(rs.getString("ai_productivity_analysis"));
        a.setAiReliabilityAnalysis(rs.getString("ai_reliability_analysis"));
        a.setAiConsistencyAnalysis(rs.getString("ai_consistency_analysis"));
        a.setAiProblemSolving(rs.getString("ai_problem_solving"));
        Timestamp ag = rs.getTimestamp("ai_generated_at"); if (ag != null) a.setAiGeneratedAt(ag.toLocalDateTime());
        setNullableDouble(rs, "manager_score", a::setManagerScore);
        a.setManagerGrade(rs.getString("manager_grade")); a.setManagerDecision(rs.getString("manager_decision"));
        a.setManagerRemark(rs.getString("manager_remark"));
        Timestamp mr = rs.getTimestamp("manager_reviewed_at"); if (mr != null) a.setManagerReviewedAt(mr.toLocalDateTime());
        a.setFinalStatus(rs.getString("final_status"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) a.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at"); if (ua != null) a.setUpdatedAt(ua.toLocalDateTime());
        return a;
    }

    private void setNullableDouble(ResultSet rs, String col, java.util.function.Consumer<Double> setter) throws SQLException {
        double v = rs.getDouble(col);
        setter.accept(rs.wasNull() ? null : v);
    }

    private void setDoubleOrNull(PreparedStatement ps, int idx, Double value) throws SQLException {
        if (value != null) ps.setDouble(idx, value); else ps.setNull(idx, Types.DOUBLE);
    }
}
