<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Appraisal Periods — TaskVoice</title>
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
            <h1 class="page-title">Appraisal Periods</h1>
            <p class="page-subtitle">Run AI performance analysis and finalize team evaluations</p>
          </div>
          <button onclick="document.getElementById('createPeriodModal').style.display='flex'" class="btn btn-primary">
            + New Period
          </button>
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
                <th>Period Title</th>
                <th>Type</th>
                <th>Timeline</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="p" items="${periods}">
                <tr>
                  <td style="font-weight: 600;">
                    <a href="${pageContext.request.contextPath}/manager/appraisals?periodId=${p.id}"><c:out value="${p.title}"/></a>
                  </td>
                  <td><c:out value="${p.periodType}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${p.startDate}"/> → <c:out value="${p.endDate}"/></td>
                  <td><span class="status-pill status-completed"><c:out value="${p.status}"/></span></td>
                  <td>
                    <button onclick="openTriggerModal(${p.id})" class="btn btn-secondary" style="padding:4px 8px; font-size:11px;">Run AI Appraisal</button>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- Create Period Modal -->
  <div id="createPeriodModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 460px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 20px;">Create Appraisal Period</h2>
      <form action="${pageContext.request.contextPath}/manager/appraisals" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="createPeriod"/>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">PERIOD TITLE</label>
          <input type="text" name="title" class="form-control" placeholder="Q3 2026 Appraisal" required/>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">TYPE</label>
          <select name="periodType" class="form-control">
            <option value="QUARTERLY">QUARTERLY</option>
            <option value="MONTHLY">MONTHLY</option>
            <option value="HALF_YEARLY">HALF_YEARLY</option>
            <option value="YEARLY">YEARLY</option>
            <option value="CUSTOM">CUSTOM</option>
          </select>
        </div>

        <div style="display:flex; gap:16px; margin-bottom: 24px;">
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">START DATE</label>
            <input type="date" name="startDate" class="form-control" required/>
          </div>
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">END DATE</label>
            <input type="date" name="endDate" class="form-control" required/>
          </div>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('createPeriodModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Create Period</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Trigger AI Modal -->
  <div id="triggerAiModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 440px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 12px;">Run AI Appraisal</h2>
      <p style="color: var(--text-secondary); font-size: 13px; margin-bottom: 20px;">
        Gemini will analyze task history, deadline adherence, problems faced, and manager grades to generate a performance narrative.
      </p>
      <form action="${pageContext.request.contextPath}/manager/appraisals" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="triggerAi"/>
        <input type="hidden" id="targetPeriodId" name="periodId" value=""/>

        <div style="margin-bottom: 24px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">SELECT EMPLOYEE</label>
          <select name="employeeId" class="form-control" required>
            <c:forEach var="m" items="${team}">
              <option value="${m.id}"><c:out value="${m.name}"/> (<c:out value="${m.employeeNo}"/></option>
            </c:forEach>
          </select>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('triggerAiModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Run AI Analysis</button>
        </div>
      </form>
    </div>
  </div>

  <script>
    function openTriggerModal(periodId) {
      document.getElementById('targetPeriodId').value = periodId;
      document.getElementById('triggerAiModal').style.display = 'flex';
    }
  </script>
</body>
</html>
