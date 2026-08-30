<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Calendar & Daily Logs — TaskVoice</title>
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
            <h1 class="page-title">Calendar & Daily Logs</h1>
            <p class="page-subtitle">Logged activities and upcoming deadlines for <c:out value="${selectedDate}"/></p>
          </div>
          <form action="${pageContext.request.contextPath}/employee/calendar" method="GET" style="display:flex; gap:12px;">
            <input type="date" name="date" value="${selectedDate}" class="form-control" onchange="this.form.submit()"/>
          </form>
        </div>

        <div class="bento-grid" style="padding: 0;">
          <!-- Daily Log Summary Panel -->
          <div class="bento-col-8">
            <div class="glass-card">
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <h3 style="font-size: 16px;">Daily Work Log (<c:out value="${selectedDate}"/>)</h3>
                <span class="mono" style="font-weight: 700; font-size: 16px; color: var(--accent-emerald);"><c:out value="${totalHours}"/> hrs logged</span>
              </div>

              <div class="table-container">
                <table class="enterprise-table">
                  <thead>
                    <tr>
                      <th>Activity Title</th>
                      <th>Category</th>
                      <th>Task Link</th>
                      <th>Duration</th>
                      <th>Source</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:choose>
                      <c:when test="${empty entries}">
                        <tr>
                          <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">
                            No timesheet entries logged for this date.
                          </td>
                        </tr>
                      </c:when>
                      <c:otherwise>
                        <c:forEach var="e" items="${entries}">
                          <tr>
                            <td style="font-weight: 600;"><c:out value="${e.title}"/></td>
                            <td><span class="status-pill status-in_progress" style="font-size: 11px;"><c:out value="${e.categoryName != null ? e.categoryName : 'General'}"/></span></td>
                            <td style="color: var(--text-secondary); font-size: 13px;"><c:out value="${e.taskTitle != null ? e.taskTitle : '-'}"/></td>
                            <td class="mono" style="font-weight: 600;"><c:out value="${e.durationHours}"/> hrs</td>
                            <td>
                              <span class="status-pill ${e.voiceDerived ? 'status-completed' : 'status-under_review'}" style="font-size: 10px;">
                                <c:out value="${e.voiceDerived ? 'VOICE' : 'MANUAL'}"/>
                              </span>
                            </td>
                          </tr>
                        </c:forEach>
                      </c:otherwise>
                    </c:choose>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- Upcoming Deadlines Sidebar Panel -->
          <div class="bento-col-4">
            <div class="glass-card">
              <h3 style="font-size: 16px; margin-bottom: 16px;">Upcoming Task Deadlines</h3>
              <div style="display:flex; flex-direction:column; gap:12px;">
                <c:forEach var="t" items="${dueSoonTasks}">
                  <div style="padding: 12px; background: rgba(255,255,255,0.02); border: 1px solid var(--border); border-radius: var(--radius-sm);">
                    <div style="font-weight: 600; font-size: 13px;"><c:out value="${t.title}"/></div>
                    <div style="display:flex; justify-content:space-between; margin-top: 6px; font-size: 11px;">
                      <span style="color: var(--text-secondary);"><c:out value="${t.projectTitle}"/></span>
                      <span class="mono" style="color: var(--accent-amber);">Due: <c:out value="${t.dueDate}"/></span>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  </div>
</body>
</html>
