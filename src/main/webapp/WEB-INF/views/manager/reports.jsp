<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Reports & Exports — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 600px;">
        <div class="page-header">
          <div>
            <h1 class="page-title">Team Reports & Exports</h1>
            <p class="page-subtitle">Export official corporate work statements and raw CSV data</p>
          </div>
        </div>

        <div class="glass-card">
          <form action="${pageContext.request.contextPath}/manager/reports" method="GET">
            <div style="margin-bottom: 20px;">
              <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">EXPORT FORMAT</label>
              <select name="format" class="form-control">
                <option value="pdf">Corporate Work Statement (PDF)</option>
                <option value="csv">Full Tabular Dataset (CSV)</option>
              </select>
            </div>

            <div style="display:flex; gap:16px; margin-bottom: 24px;">
              <div style="flex:1;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FROM DATE</label>
                <input type="date" name="from" class="form-control" value="2026-07-01"/>
              </div>
              <div style="flex:1;">
                <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">TO DATE</label>
                <input type="date" name="to" class="form-control" value="2026-08-31"/>
              </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">Download Official Export</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
