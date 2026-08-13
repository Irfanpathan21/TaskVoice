<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${project.title}"/> — Project Detail</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl);">
        <div class="page-header">
          <div>
            <h1 class="page-title"><c:out value="${project.title}"/></h1>
            <p class="page-subtitle"><c:out value="${project.description}"/></p>
          </div>
          <span class="status-pill status-${project.status.toLowerCase()}"><c:out value="${project.status}"/></span>
        </div>

        <!-- Visual Project Lifecycle Stepper (§13) -->
        <div class="glass-card" style="margin-bottom: 24px;">
          <h4 style="font-size: 12px; text-transform: uppercase; color: var(--text-muted); margin-bottom: 16px;">Project Lifecycle Trajectory</h4>
          <div style="display: flex; justify-content: space-between; align-items: center; position: relative;">
            <div style="text-align: center;">
              <span class="status-pill status-completed">Created</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill ${project.totalTasks > 0 ? 'status-completed' : 'status-in_progress'}">Task Assigned</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill status-in_progress">Progress Updates (${project.totalTasks})</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill ${project.completedTasks == project.totalTasks && project.totalTasks > 0 ? 'status-completed' : 'status-in_progress'}">Task Completed</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill ${project.completed ? 'status-completed' : 'status-in_progress'}">Project Completed</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill ${project.hasSentiment() ? 'status-completed' : 'status-in_progress'}">AI Analysis</span>
            </div>
            <div style="flex:1; height: 2px; background: var(--border);"></div>
            <div style="text-align: center;">
              <span class="status-pill status-in_progress">Appraisal Feed-in</span>
            </div>
          </div>
        </div>

        <!-- AI Summary & Sentiment Panel (if completed) -->
        <c:if test="${project.completed && project.hasSentiment()}">
          <div class="glass-card" style="margin-bottom: 24px; border-color: rgba(59, 130, 246, 0.3); background: rgba(59, 130, 246, 0.05);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px;">
              <h3 style="font-size: 16px; color: var(--accent-blue);">AI Completion Analysis & Sentiment</h3>
              <span class="status-pill ${project.aiSentiment == 'POSITIVE' ? 'status-completed' : (project.aiSentiment == 'NEGATIVE' ? 'status-blocked' : 'status-under_review')}">
                Sentiment: <c:out value="${project.aiSentiment}"/> (<c:out value="${project.aiSentimentConfidence}"/>% confidence)
              </span>
            </div>
            <p style="font-size: 14px; color: var(--text-primary); margin-bottom: 12px;">
              <strong>Summary:</strong> <c:out value="${project.aiSummary}"/>
            </p>
            <p style="font-size: 13px; color: var(--text-secondary);">
              <strong>Sentiment Rationale:</strong> <c:out value="${project.aiSentimentExplanation}"/>
            </p>
          </div>
        </c:if>

        <!-- Tasks Table -->
        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Project Tasks</h3>
          <div class="table-container">
            <table class="enterprise-table">
              <thead>
                <tr>
                  <th>Task Title</th>
                  <th>Assignee</th>
                  <th>Priority</th>
                  <th>Due Date</th>
                  <th>Status</th>
                  <th>Grade</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${tasks}">
                  <tr>
                    <td style="font-weight: 600;">
                      <a href="${pageContext.request.contextPath}/manager/tasks?id=${t.id}"><c:out value="${t.title}"/></a>
                    </td>
                    <td><c:out value="${t.assigneeName}"/></td>
                    <td><c:out value="${t.priority}"/></td>
                    <td class="mono" style="font-size: 12px;"><c:out value="${t.dueDate}"/></td>
                    <td><span class="status-pill status-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span></td>
                    <td><c:out value="${t.managerGrade != null ? t.gradeDisplay : '-'}"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
