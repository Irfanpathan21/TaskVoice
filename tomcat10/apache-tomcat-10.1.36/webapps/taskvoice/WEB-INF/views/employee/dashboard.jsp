<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Employee Dashboard — TaskVoice</title>
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
              <h1 class="page-title">My Execution Workspace</h1>
              <p class="page-subtitle">Log daily work by voice, track assigned tasks, and view feedback</p>
            </div>
            <a href="${pageContext.request.contextPath}/employee/voice-timesheet" class="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/></svg>
              Record Work
            </a>
          </div>
        </div>

        <!-- Metric Cards -->
        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">TODAY'S LOGGED HOURS</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${todayHours}"/> hrs</div>
          </div>
        </div>

        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">MY ACTIVE PROJECTS</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${myProjectsCount}"/></div>
          </div>
        </div>

        <div class="bento-col-4">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">TASKS DUE SOON</div>
            <div style="font-size: 32px; font-weight: 700; margin-top: 8px; color: var(--accent-amber);" class="font-display"><c:out value="${dueSoonTasks.size()}"/></div>
          </div>
        </div>

        <!-- My Assigned Tasks Table -->
        <div class="bento-col-12">
          <div class="glass-card">
            <h3 style="font-size: 16px; margin-bottom: 16px;">My Assigned Tasks</h3>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Task Title</th>
                    <th>Project</th>
                    <th>Due Date</th>
                    <th>Progress</th>
                    <th>Status</th>
                    <th>Manager Feedback</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="t" items="${myTasks}">
                    <tr>
                      <td style="font-weight: 600;">
                        <a href="${pageContext.request.contextPath}/employee/tasks?id=${t.id}"><c:out value="${t.title}"/></a>
                      </td>
                      <td style="color: var(--text-secondary);"><c:out value="${t.projectTitle}"/></td>
                      <td class="mono" style="font-size: 12px;"><c:out value="${t.dueDate}"/></td>
                      <td class="mono"><c:out value="${t.completionPct}"/>%</td>
                      <td><span class="status-pill status-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span></td>
                      <td>
                        <c:choose>
                          <c:when test="${t.managerGrade != null}">
                            <span class="status-pill status-completed"><c:out value="${t.gradeDisplay}"/></span>
                          </c:when>
                          <c:otherwise><span style="font-size: 12px; color: var(--text-muted);">-</span></c:otherwise>
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
