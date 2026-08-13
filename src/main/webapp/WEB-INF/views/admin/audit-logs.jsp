<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Audit Logs — TaskVoice</title>
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
            <h1 class="page-title">System Audit Logs</h1>
            <p class="page-subtitle">Security audit trail of all organizational events</p>
          </div>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Target</th>
                <th>Detail</th>
                <th>IP Address</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="log" items="${logs}">
                <tr>
                  <td class="mono"><c:out value="${log.id}"/></td>
                  <td style="font-weight: 600;"><c:out value="${log.actorName}"/></td>
                  <td><span class="status-pill status-in_progress" style="font-size:11px;"><c:out value="${log.action}"/></span></td>
                  <td><c:out value="${log.entityType != null ? log.entityType : '-'}"/> #<c:out value="${log.entityId}"/></td>
                  <td style="color: var(--text-secondary); font-size: 13px;"><c:out value="${log.detail}"/></td>
                  <td class="mono" style="font-size: 12px; color: var(--text-muted);"><c:out value="${log.ipAddress}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${log.createdAt}"/></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
