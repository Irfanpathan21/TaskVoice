<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Team — TaskVoice</title>
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
            <h1 class="page-title">Team Members</h1>
            <p class="page-subtitle">Assigned employees under your supervision</p>
          </div>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Emp No</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Joining Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="emp" items="${team}">
                <tr>
                  <td class="mono"><c:out value="${emp.employeeNo}"/></td>
                  <td style="font-weight: 600;"><c:out value="${emp.name}"/></td>
                  <td style="color: var(--text-secondary);"><c:out value="${emp.email}"/></td>
                  <td><c:out value="${emp.departmentName != null ? emp.departmentName : '-'}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${emp.joiningDate}"/></td>
                  <td><span class="status-pill status-completed"><c:out value="${emp.status}"/></span></td>
                  <td>
                    <a href="${pageContext.request.contextPath}/manager/team?id=${emp.id}" class="btn btn-secondary" style="padding:4px 10px; font-size:11px;">View History</a>
                  </td>
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
