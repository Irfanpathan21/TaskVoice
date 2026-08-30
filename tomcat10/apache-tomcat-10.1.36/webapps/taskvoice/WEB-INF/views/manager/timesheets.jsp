<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Team Timesheets — TaskVoice</title>
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
            <h1 class="page-title">Team Timesheets & Exports</h1>
            <p class="page-subtitle">Select team members to inspect daily work entries and download official timesheets</p>
          </div>
        </div>

        <!-- Filter & Download Control Bar -->
        <div class="glass-card" style="margin-bottom: 24px; padding: 20px;">
          <form action="${pageContext.request.contextPath}/manager/timesheets" method="GET" style="display:flex; flex-wrap:wrap; gap:16px; align-items:flex-end;">
            <div style="flex: 1; min-width: 200px;">
              <label style="display:block; font-size:11px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">SELECT EMPLOYEE</label>
              <select name="employeeId" class="form-control" onchange="this.form.submit()">
                <option value="">-- All Team Members --</option>
                <c:forEach var="m" items="${team}">
                  <option value="${m.id}" ${selectedEmployeeId == m.id ? 'selected' : ''}>
                    <c:out value="${m.name}"/> (<c:out value="${m.employeeNo}"/>)
                  </option>
                </c:forEach>
              </select>
            </div>

            <div>
              <label style="display:block; font-size:11px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FROM DATE</label>
              <input type="date" name="from" value="${fromDate}" class="form-control"/>
            </div>

            <div>
              <label style="display:block; font-size:11px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">TO DATE</label>
              <input type="date" name="to" value="${toDate}" class="form-control"/>
            </div>

            <button type="submit" class="btn btn-primary" style="padding: 10px 16px;">Filter Entries</button>

            <!-- Export Buttons -->
            <div style="margin-left: auto; display:flex; gap: 8px;">
              <a href="${pageContext.request.contextPath}/manager/reports?format=pdf&from=${fromDate}&to=${toDate}&employeeId=${selectedEmployeeId}" class="btn btn-secondary" style="font-size: 12px; color: var(--accent-blue);">
                📄 Download PDF
              </a>
              <a href="${pageContext.request.contextPath}/manager/reports?format=csv&from=${fromDate}&to=${toDate}&employeeId=${selectedEmployeeId}" class="btn btn-secondary" style="font-size: 12px; color: var(--accent-emerald);">
                📊 Download CSV
              </a>
            </div>
          </form>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Employee</th>
                <th>Activity Title</th>
                <th>Category</th>
                <th>Project / Task Link</th>
                <th>Duration</th>
                <th>Source</th>
              </tr>
            </thead>
            <tbody>
              <c:choose>
                <c:when test="${empty entries}">
                  <tr>
                    <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 30px;">
                      No timesheet entries found for the selected filter.
                    </td>
                  </tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="e" items="${entries}">
                    <tr>
                      <td class="mono" style="font-size: 12px;"><c:out value="${e.entryDate}"/></td>
                      <td style="font-weight: 600;"><c:out value="${e.userName}"/></td>
                      <td><c:out value="${e.title}"/></td>
                      <td><span class="status-pill status-in_progress" style="font-size: 11px;"><c:out value="${e.categoryName != null ? e.categoryName : 'General'}"/></span></td>
                      <td style="color: var(--text-secondary); font-size: 13px;"><c:out value="${e.projectTitle != null ? e.projectTitle : '-'}"/></td>
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
  </div>
</body>
</html>
