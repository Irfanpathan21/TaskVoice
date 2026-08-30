<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Tasks — TaskVoice</title>
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
            <h1 class="page-title">My Assigned Tasks</h1>
            <p class="page-subtitle">Submit progress updates and view manager grades and remarks</p>
          </div>
        </div>

        <c:if test="${not empty sessionScope.flashMessage}">
          <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-emerald); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${sessionScope.flashMessage}"/>
          </div>
          <c:remove var="flashMessage" scope="session"/>
        </c:if>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Emp No</th>
                <th>Employee Name</th>
                <th>Project</th>
                <th>Task</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Progress %</th>
                <th>Status</th>
                <th>Manager Grade</th>
                <th>Manager Remark</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="t" items="${tasks}">
                <tr>
                  <td class="mono"><c:out value="${t.assigneeNo}"/></td>
                  <td style="font-weight: 600;"><c:out value="${t.assigneeName}"/></td>
                  <td style="color: var(--text-secondary);"><c:out value="${t.projectTitle}"/></td>
                  <td style="font-weight: 600;">
                    <a href="${pageContext.request.contextPath}/employee/tasks?id=${t.id}"><c:out value="${t.title}"/></a>
                  </td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${t.startDate}"/></td>
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
                  <td style="font-size: 12px; color: var(--text-secondary); max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                    <c:out value="${t.managerRemark != null ? t.managerRemark : '-'}"/>
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
