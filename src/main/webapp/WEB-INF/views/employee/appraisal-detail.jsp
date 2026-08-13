<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Appraisal Detail — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 800px;">
        <div class="page-header">
          <div>
            <h1 class="page-title">Appraisal Results: <c:out value="${appraisal.periodTitle}"/></h1>
            <p class="page-subtitle">Evaluated by: <c:out value="${appraisal.managerName}"/></p>
          </div>
          <span class="status-pill status-completed"><c:out value="${appraisal.finalStatus}"/></span>
        </div>

        <div class="glass-card" style="margin-bottom: 24px; border-color: rgba(16, 185, 129, 0.3); background: rgba(16, 185, 129, 0.05);">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px;">
            <h3 style="font-size: 16px; color: var(--accent-emerald);">Final Evaluation Decision</h3>
            <span class="status-pill status-completed" style="font-size: 16px; font-weight: 700;">Grade: <c:out value="${appraisal.managerGradeDisplay}"/> (${appraisal.managerScore}/100)</span>
          </div>
          <p style="font-size: 14px; color: var(--text-primary);">
            <strong>Manager Remark:</strong> "<c:out value="${appraisal.managerRemark}"/>"
          </p>
        </div>

        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">AI Analysis Narrative & Insights</h3>
          <div style="font-size: 14px; color: var(--text-secondary); display:flex; flex-direction:column; gap:12px;">
            <p><strong>Overall Summary:</strong> <c:out value="${appraisal.aiSummary}"/></p>
            <p><strong>Identified Strengths:</strong> <c:out value="${appraisal.aiStrengths}"/></p>
            <p><strong>Growth & Improvement Areas:</strong> <c:out value="${appraisal.aiImprovements}"/></p>
            <p><strong>Productivity & Reliability:</strong> <c:out value="${appraisal.aiProductivityAnalysis}"/></p>
          </div>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
