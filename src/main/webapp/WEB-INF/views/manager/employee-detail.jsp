<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${employee.name}"/> — Employee Profile</title>
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
            <h1 class="page-title"><c:out value="${employee.name}"/></h1>
            <p class="page-subtitle">Employee No: <span class="mono"><c:out value="${employee.employeeNo}"/></span> | Email: <c:out value="${employee.email}"/></p>
          </div>
          <span class="status-pill status-completed"><c:out value="${employee.status}"/></span>
        </div>

        <!-- Task History -->
        <div class="glass-card" style="margin-bottom: 24px;">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Assigned Tasks</h3>
          <div class="table-container">
            <table class="enterprise-table">
              <thead>
                <tr>
                  <th>Task Title</th>
                  <th>Project</th>
                  <th>Status</th>
                  <th>Progress</th>
                  <th>Grade</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${tasks}">
                  <tr>
                    <td style="font-weight: 600;">
                      <a href="${pageContext.request.contextPath}/manager/tasks?id=${t.id}"><c:out value="${t.title}"/></a>
                    </td>
                    <td style="color: var(--text-secondary);"><c:out value="${t.projectTitle}"/></td>
                    <td><span class="status-pill status-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span></td>
                    <td class="mono"><c:out value="${t.completionPct}"/>%</td>
                    <td><c:out value="${t.managerGrade != null ? t.gradeDisplay : '-'}"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Appraisal History -->
        <div class="glass-card">
          <h3 style="font-size: 16px; margin-bottom: 16px;">Appraisal History</h3>
          <div class="table-container">
            <table class="enterprise-table">
              <thead>
                <tr>
                  <th>Period</th>
                  <th>AI Score</th>
                  <th>Final Manager Grade</th>
                  <th>Decision</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="a" items="${appraisals}">
                  <tr>
                    <td style="font-weight: 600;"><c:out value="${a.periodTitle}"/></td>
                    <td class="mono"><c:out value="${a.aiScore != null ? a.aiScore : '-'}"/></td>
                    <td><c:out value="${a.managerGrade != null ? a.managerGradeDisplay : '-'}"/></td>
                    <td><span class="status-pill status-completed"><c:out value="${a.managerDecision != null ? a.managerDecision : 'PENDING'}"/></span></td>
                    <td><span class="status-pill status-in_progress"><c:out value="${a.finalStatus}"/></span></td>
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
