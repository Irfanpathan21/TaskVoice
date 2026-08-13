<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Team Analytics — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl);">
        <div class="page-header">
          <div>
            <h1 class="page-title">Team Performance Analytics</h1>
            <p class="page-subtitle">Data-backed coaching insights for your team</p>
          </div>
        </div>

        <div class="bento-grid" style="padding: 0;">
          <div class="bento-col-6">
            <div class="glass-card">
              <h3 style="font-size: 16px; margin-bottom: 16px;">Task Status Distribution</h3>
              <canvas id="taskStatusChart" height="200"></canvas>
            </div>
          </div>

          <div class="bento-col-6">
            <div class="glass-card">
              <h3 style="font-size: 16px; margin-bottom: 16px;">Overdue vs On-Time Completion</h3>
              <canvas id="deadlineChart" height="200"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <script>
    const ctx1 = document.getElementById('taskStatusChart').getContext('2d');
    new Chart(ctx1, {
      type: 'doughnut',
      data: {
        labels: ['Completed', 'In Progress', 'Not Started', 'Under Review'],
        datasets: [{
          data: [12, 8, 5, 3],
          backgroundColor: ['#10B981', '#3B82F6', '#64748B', '#F59E0B']
        }]
      },
      options: { plugins: { legend: { labels: { color: '#94A3B8' } } } }
    });

    const ctx2 = document.getElementById('deadlineChart').getContext('2d');
    new Chart(ctx2, {
      type: 'bar',
      data: {
        labels: ['On Time', 'Overdue'],
        datasets: [{
          label: 'Tasks',
          data: [${totalTasks - overdueCount}, ${overdueCount}],
          backgroundColor: ['#10B981', '#F43F5E']
        }]
      },
      options: { plugins: { legend: { labels: { color: '#94A3B8' } } } }
    });
  </script>
</body>
</html>
