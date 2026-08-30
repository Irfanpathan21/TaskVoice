<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${task.title}"/> — Task Detail</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 900px;">
        <div class="page-header">
          <div>
            <h1 class="page-title"><c:out value="${task.title}"/></h1>
            <p class="page-subtitle">Project: <c:out value="${task.projectTitle}"/> | Due: <c:out value="${task.dueDate}"/></p>
          </div>
          <span class="status-pill status-${task.status.toLowerCase()}"><c:out value="${task.status}"/></span>
        </div>

        <!-- Manager Grade & Remark Panel (READ ONLY) -->
        <c:if test="${task.managerGrade != null}">
          <div class="glass-card" style="margin-bottom: 24px; border-color: rgba(16, 185, 129, 0.3); background: rgba(16, 185, 129, 0.05);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
              <h3 style="font-size: 16px; color: var(--accent-emerald);">Manager Feedback & Grade</h3>
              <span class="status-pill status-completed" style="font-size: 14px; font-weight: 700;">Grade: <c:out value="${task.gradeDisplay}"/> (<c:out value="${task.managerScore}"/>)</span>
            </div>
            <p style="font-size: 14px; color: var(--text-primary); margin-top: 8px;">
              <strong>Manager Remark:</strong> "<c:out value="${task.managerRemark}"/>"
            </p>
          </div>
        </c:if>

        <!-- Submit Progress Update Form -->
        <div class="glass-card" style="margin-bottom: 24px;">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Submit Progress Update</h3>
          <form action="${pageContext.request.contextPath}/employee/tasks" method="POST">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
            <input type="hidden" name="action" value="addUpdate"/>
            <input type="hidden" name="taskId" value="${task.id}"/>

            <div style="margin-bottom: 16px;">
              <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">WORK UPDATE / DESCRIPTION</label>
              <textarea name="rawText" class="form-control" rows="3" required placeholder="Describe what you worked on or completed..."></textarea>
            </div>

            <div style="display:flex; gap:16px; margin-bottom: 16px;">
              <div style="flex:1;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">COMPLETION % (0-100)</label>
                <input type="number" min="0" max="100" name="completionPct" class="form-control" value="${task.completionPct}"/>
              </div>
              <div style="flex:2;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">PROBLEMS FACED (Optional)</label>
                <input type="text" name="problemsFaced" class="form-control" placeholder="Any blockers, bugs, or delays..."/>
              </div>
            </div>

            <button type="submit" class="btn btn-primary">Submit Progress Update (AI Rephrase)</button>
          </form>
        </div>

        <!-- History of Updates -->
        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Update History (Append-Only)</h3>
          <c:forEach var="u" items="${updates}">
            <div style="padding: 14px; background: rgba(255,255,255,0.02); border: 1px solid var(--border); border-radius: var(--radius-sm); margin-bottom: 12px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                <span class="status-pill status-in_progress" style="font-size:11px;">Update #${u.updateSeq}</span>
                <span class="mono" style="font-size:11px; color:var(--text-muted);"><c:out value="${u.createdAt}"/></span>
              </div>
              <p style="font-size:14px; color:var(--text-primary); margin-bottom: 4px;">"<c:out value="${u.rawText}"/>"</p>
              <c:if test="${u.hasAiVersion()}">
                <p style="font-size:13px; color:var(--accent-blue);"><strong>AI Rephrased:</strong> <c:out value="${u.aiRephrasedText}"/></p>
              </c:if>
            </div>
          </c:forEach>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
