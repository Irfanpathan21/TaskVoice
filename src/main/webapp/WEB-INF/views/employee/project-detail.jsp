<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${project.title}"/> — Project Overview</title>
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

        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Project Tasks</h3>
          <div class="table-container">
            <table class="enterprise-table">
              <thead>
                <tr>
                  <th>Task Title</th>
                  <th>Assignee</th>
                  <th>Due Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${tasks}">
                  <tr>
                    <td style="font-weight: 600;"><c:out value="${t.title}"/></td>
                    <td><c:out value="${t.assigneeName}"/></td>
                    <td class="mono" style="font-size: 12px;"><c:out value="${t.dueDate}"/></td>
                    <td><span class="status-pill status-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span></td>
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
