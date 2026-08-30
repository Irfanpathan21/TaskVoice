<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Appraisal Review — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 1000px;">
        <div class="page-header">
          <div>
            <h1 class="page-title">Appraisal Review: <c:out value="${appraisal.employeeName}"/></h1>
            <p class="page-subtitle">Period: <c:out value="${appraisal.periodTitle}"/></p>
          </div>
          <span class="status-pill status-in_progress"><c:out value="${appraisal.finalStatus}"/></span>
        </div>

        <div class="bento-grid" style="padding: 0; margin-bottom: 24px;">
          <!-- AI Recommendation Panel (Read-only) -->
          <div class="bento-col-6">
            <div class="glass-card" style="border-color: rgba(59, 130, 246, 0.3); background: rgba(59, 130, 246, 0.04); height: 100%;">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 16px;">
                <h3 style="font-size: 16px; color: var(--accent-blue);">AI Analysis Recommendation</h3>
                <span class="status-pill status-in_progress">AI Score: <c:out value="${appraisal.aiScore}"/></span>
              </div>

              <div style="margin-bottom: 12px; font-size: 13px;">
                <strong>Suggested Grade:</strong> <c:out value="${appraisal.aiGradeDisplay}"/><br/>
                <strong>Promotion Rec:</strong> <c:out value="${appraisal.promotionRecDisplay}"/><br/>
                <strong>Increment Range:</strong> <span style="color: var(--accent-amber);"><c:out value="${appraisal.aiIncrementRange}"/></span>
              </div>

              <div style="margin-top: 16px; font-size: 13px; color: var(--text-secondary);">
                <p style="margin-bottom: 8px;"><strong>Summary:</strong> <c:out value="${appraisal.aiSummary}"/></p>
                <p style="margin-bottom: 8px;"><strong>Strengths:</strong> <c:out value="${appraisal.aiStrengths}"/></p>
                <p><strong>Improvement Areas:</strong> <c:out value="${appraisal.aiImprovements}"/></p>
              </div>

              <div style="margin-top: 20px; padding: 8px; background: rgba(255,255,255,0.03); border-radius: 4px; font-size: 11px; color: var(--text-muted);">
                AI Recommendation — Manager Decision Required. All final promotion and salary decisions rest with the human manager.
              </div>
            </div>
          </div>

          <!-- Manager Final Decision Form -->
          <div class="bento-col-6">
            <div class="glass-card" style="height: 100%;">
              <h3 style="font-size: 16px; margin-bottom: 16px;">Manager Final Decision</h3>
              <form action="${pageContext.request.contextPath}/manager/appraisals" method="POST">
                <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                <input type="hidden" name="action" value="finalize"/>
                <input type="hidden" name="appraisalId" value="${appraisal.id}"/>

                <div style="margin-bottom: 16px;">
                  <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">DECISION ACTION</label>
                  <select name="managerDecision" class="form-control" required>
                    <option value="ACCEPTED">ACCEPT — Adopt AI Recommendation</option>
                    <option value="MODIFIED">MODIFY — Adjust Score/Grade with Remark</option>
                    <option value="REJECTED">REJECT — Override AI Recommendation</option>
                  </select>
                </div>

                <div style="margin-bottom: 16px;">
                  <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FINAL MANAGER SCORE (0-100)</label>
                  <input type="number" step="0.1" name="managerScore" class="form-control" required value="${appraisal.aiScore != null ? appraisal.aiScore : 85.0}"/>
                </div>

                <div style="margin-bottom: 16px;">
                  <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FINAL GRADE</label>
                  <select name="managerGrade" class="form-control" required>
                    <option value="OUTSTANDING">Outstanding</option>
                    <option value="EXCELLENT">Excellent</option>
                    <option value="VERY_GOOD" selected>Very Good</option>
                    <option value="GOOD">Good</option>
                    <option value="AVERAGE">Average</option>
                    <option value="NEEDS_IMPROVEMENT">Needs Improvement</option>
                  </select>
                </div>

                <div style="margin-bottom: 24px;">
                  <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FINAL MANAGER REMARK</label>
                  <textarea name="managerRemark" class="form-control" rows="4" required placeholder="Mandatory manager explanation for final decision..."><c:out value="${appraisal.managerRemark}"/></textarea>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%;">Finalize Appraisal Decision</button>
              </form>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
