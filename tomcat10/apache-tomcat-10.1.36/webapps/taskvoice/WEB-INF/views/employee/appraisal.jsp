<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Appraisals — TaskVoice</title>
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
            <h1 class="page-title">My Performance Appraisals</h1>
            <p class="page-subtitle">Read-only view of finalized period appraisals</p>
          </div>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Appraisal Period</th>
                <th>Final Manager Score</th>
                <th>Final Grade</th>
                <th>Manager Decision</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="a" items="${appraisals}">
                <tr>
                  <td style="font-weight: 600;"><c:out value="${a.periodTitle}"/></td>
                  <td class="mono"><c:out value="${a.managerScore != null ? a.managerScore : '-'}"/></td>
                  <td><c:out value="${a.managerGrade != null ? a.managerGradeDisplay : '-'}"/></td>
                  <td><span class="status-pill status-completed"><c:out value="${a.managerDecision != null ? a.managerDecision : 'PENDING'}"/></span></td>
                  <td><span class="status-pill status-${a.finalStatus.toLowerCase()}"><c:out value="${a.finalStatus}"/></span></td>
                  <td>
                    <a href="${pageContext.request.contextPath}/employee/appraisal?id=${a.id}" class="btn btn-secondary" style="padding:4px 8px; font-size:11px;">View Results</a>
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
