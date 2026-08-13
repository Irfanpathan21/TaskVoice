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
            <p class="page-subtitle">Assigned to: <strong><c:out value="${task.assigneeName}"/></strong> (<c:out value="${task.assigneeNo}"/>) | Project: <c:out value="${task.projectTitle}"/></p>
          </div>
          <span class="status-pill status-${task.status.toLowerCase()}"><c:out value="${task.status}"/></span>
        </div>

        <!-- Manager Grade Form Card -->
        <div class="glass-card" style="margin-bottom: 24px; border-color: rgba(59, 130, 246, 0.3);">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Manager Evaluation & Grading</h3>
          <form action="${pageContext.request.contextPath}/manager/tasks" method="POST">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
            <input type="hidden" name="action" value="grade"/>
            <input type="hidden" name="taskId" value="${task.id}"/>

            <div style="display: flex; gap: 16px; margin-bottom: 16px;">
              <div style="flex: 1;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">GRADE</label>
                <select name="grade" class="form-control" required>
                  <option value="A_PLUS" ${task.managerGrade == 'A_PLUS' ? 'selected' : ''}>A+ (Outstanding)</option>
                  <option value="A" ${task.managerGrade == 'A' ? 'selected' : ''}>A (Excellent)</option>
                  <option value="B_PLUS" ${task.managerGrade == 'B_PLUS' ? 'selected' : ''}>B+ (Very Good)</option>
                  <option value="B" ${task.managerGrade == 'B' ? 'selected' : ''}>B (Good)</option>
                  <option value="C" ${task.managerGrade == 'C' ? 'selected' : ''}>C (Average)</option>
                  <option value="NEEDS_IMPROVEMENT" ${task.managerGrade == 'NEEDS_IMPROVEMENT' ? 'selected' : ''}>Needs Improvement</option>
                </select>
              </div>
              <div style="flex: 1;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">SCORE (0-100)</label>
                <input type="number" step="0.1" name="score" class="form-control" required value="${task.managerScore != null ? task.managerScore : 85.0}"/>
              </div>
            </div>

            <div style="margin-bottom: 16px;">
              <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">MANAGER REMARK</label>
              <textarea name="remark" class="form-control" rows="3" required><c:out value="${task.managerRemark}"/></textarea>
            </div>

            <button type="submit" class="btn btn-primary">Submit Grade & Remark</button>
          </form>
        </div>

        <!-- Task Progress Updates Feed -->
        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Progress Updates History</h3>
          <c:choose>
            <c:when test="${empty updates}">
              <p style="color: var(--text-muted); font-size: 13px;">No progress updates submitted for this task yet.</p>
            </c:when>
            <c:otherwise>
              <div style="display: flex; flex-direction: column; gap: 16px;">
                <c:forEach var="u" items="${updates}">
                  <div style="padding: 16px; background: rgba(255,255,255,0.02); border: 1px solid var(--border); border-radius: var(--radius-sm);">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                      <span class="status-pill status-in_progress" style="font-size: 11px;">Update #${u.updateSeq}</span>
                      <span class="mono" style="font-size: 11px; color: var(--text-muted);"><c:out value="${u.createdAt}"/></span>
                    </div>
                    <div style="font-size: 14px; color: var(--text-primary); margin-bottom: 6px;">
                      <strong>Spoken/Typed:</strong> "<c:out value="${u.rawText}"/>"
                    </div>
                    <c:if test="${u.hasAiVersion()}">
                      <div style="font-size: 13px; color: var(--accent-blue); margin-bottom: 6px;">
                        <strong>AI Rephrased:</strong> <c:out value="${u.aiRephrasedText}"/>
                      </div>
                    </c:if>
                    <c:if test="${u.hasProblems()}">
                      <div style="font-size: 13px; color: var(--accent-rose); background: rgba(244, 63, 94, 0.08); padding: 8px; border-radius: 4px; margin-top: 6px;">
                        <strong>Problems Faced:</strong> <c:out value="${u.problemsFaced}"/>
                      </div>
                    </c:if>
                  </div>
                </c:forEach>
              </div>
            </c:otherwise>
          </c:choose>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
