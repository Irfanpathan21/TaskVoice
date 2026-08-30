<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Manager Dashboard — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div class="bento-grid">
        <div class="bento-col-12">
          <div class="page-header">
            <div>
              <h1 class="page-title">Team Performance Overview</h1>
              <p class="page-subtitle">Real-time status of tasks, projects, and team output</p>
            </div>
            <a href="${pageContext.request.contextPath}/manager/projects" class="btn btn-primary">+ New Project</a>
          </div>
        </div>

        <!-- Metric Cards -->
        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">TEAM MEMBERS</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${teamSize}"/></div>
          </div>
        </div>

        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">ACTIVE PROJECTS</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${activeProjectsCount}"/></div>
          </div>
        </div>

        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">PENDING EVALUATIONS</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px; color: var(--accent-amber);" class="font-display"><c:out value="${pendingGradesCount}"/></div>
          </div>
        </div>

        <!-- Overdue Tasks Warning Panel -->
        <c:if test="${not empty overdueTasks}">
          <div class="bento-col-12">
            <div class="glass-card" style="border-color: rgba(244, 63, 94, 0.3); background: rgba(244, 63, 94, 0.05);">
              <h3 style="font-size: 16px; color: var(--accent-rose); margin-bottom: 12px;">Overdue Tasks (${overdueTasks.size()})</h3>
              <div class="table-container">
                <table class="enterprise-table">
                  <thead>
                    <tr>
                      <th>Task</th>
                      <th>Assignee</th>
                      <th>Due Date</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="t" items="${overdueTasks}">
                      <tr>
                        <td style="font-weight: 600;"><c:out value="${t.title}"/></td>
                        <td><c:out value="${t.assigneeName}"/></td>
                        <td class="mono" style="color: var(--accent-rose);"><c:out value="${t.dueDate}"/></td>
                        <td><span class="status-pill status-overdue"><c:out value="${t.status}"/></span></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </c:if>

        <!-- Active Projects Panel -->
        <div class="bento-col-12">
          <div class="glass-card">
            <h3 style="font-size: 16px; margin-bottom: 16px;">Active Projects</h3>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Project</th>
                    <th>Timeline</th>
                    <th>Tasks</th>
                    <th>Status</th>
                    <th>AI Sentiment</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="p" items="${recentProjects}">
                    <tr>
                      <td style="font-weight: 600;">
                        <a href="${pageContext.request.contextPath}/manager/projects?id=${p.id}"><c:out value="${p.title}"/></a>
                      </td>
                      <td class="mono" style="font-size: 12px;"><c:out value="${p.startDate}"/> → <c:out value="${p.endDate}"/></td>
                      <td class="mono"><c:out value="${p.completedTasks}"/> / <c:out value="${p.totalTasks}"/></td>
                      <td><span class="status-pill status-in_progress"><c:out value="${p.status}"/></span></td>
                      <td>
                        <c:choose>
                          <c:when test="${p.hasSentiment()}">
                            <span class="status-pill ${p.aiSentiment == 'POSITIVE' ? 'status-completed' : (p.aiSentiment == 'NEGATIVE' ? 'status-blocked' : 'status-under_review')}">
                              <c:out value="${p.aiSentiment}"/> (<c:out value="${p.aiSentimentConfidence}"/>%)
                            </span>
                          </c:when>
                          <c:otherwise>
                            <span style="font-size: 12px; color: var(--text-muted);">Generated on completion</span>
                          </c:otherwise>
                        </c:choose>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
