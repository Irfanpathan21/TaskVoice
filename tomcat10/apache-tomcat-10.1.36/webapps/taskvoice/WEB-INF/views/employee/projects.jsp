<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Projects — TaskVoice</title>
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
            <h1 class="page-title">My Assigned Projects</h1>
            <p class="page-subtitle">Projects you are actively contributing to</p>
          </div>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Project Title</th>
                <th>Manager</th>
                <th>Timeline</th>
                <th>Status</th>
                <th>My Progress</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="p" items="${projects}">
                <tr>
                  <td style="font-weight: 600;">
                    <a href="${pageContext.request.contextPath}/employee/projects?id=${p.id}"><c:out value="${p.title}"/></a>
                  </td>
                  <td><c:out value="${p.managerName}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${p.startDate}"/> → <c:out value="${p.endDate}"/></td>
                  <td><span class="status-pill status-${p.status.toLowerCase()}"><c:out value="${p.status}"/></span></td>
                  <td class="mono"><c:out value="${String.format('%.0f', p.progressPct)}"/>%</td>
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
