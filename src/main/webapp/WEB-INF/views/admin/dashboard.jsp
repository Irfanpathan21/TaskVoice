<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Admin Dashboard — TaskVoice</title>
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
              <h1 class="page-title">Organization Dashboard</h1>
              <p class="page-subtitle">Org-wide metrics and system administration</p>
            </div>
          </div>
        </div>

        <!-- Metric Cards -->
        <div class="bento-col-6">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">TOTAL EMPLOYEES</div>
            <div style="font-size: 36px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${totalEmployees}"/></div>
          </div>
        </div>

        <div class="bento-col-6">
          <div class="glass-card">
            <div style="font-size: 12px; font-weight: 600; color: var(--text-secondary);">TOTAL MANAGERS</div>
            <div style="font-size: 36px; font-weight: 700; margin-top: 8px;" class="font-display"><c:out value="${totalManagers}"/></div>
          </div>
        </div>

        <!-- Departments Headcount -->
        <div class="bento-col-6">
          <div class="glass-card">
            <h3 style="font-size: 16px; margin-bottom: 16px;">Departments Overview</h3>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Department</th>
                    <th>Headcount</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="dept" items="${departments}">
                    <tr>
                      <td style="font-weight: 500;"><c:out value="${dept.name}"/></td>
                      <td class="mono"><c:out value="${dept.headcount}"/></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Recent Audit Log Events -->
        <div class="bento-col-6">
          <div class="glass-card">
            <h3 style="font-size: 16px; margin-bottom: 16px;">Recent Audit Events</h3>
            <div class="table-container">
              <table class="enterprise-table">
                <thead>
                  <tr>
                    <th>Actor</th>
                    <th>Action</th>
                    <th>Detail</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="log" items="${recentAuditLogs}">
                    <tr>
                      <td style="font-size: 13px;"><c:out value="${log.actorName}"/></td>
                      <td><span class="status-pill status-in_progress" style="font-size: 10px;"><c:out value="${log.action}"/></span></td>
                      <td style="font-size: 12px; color: var(--text-secondary);"><c:out value="${log.detail}"/></td>
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
