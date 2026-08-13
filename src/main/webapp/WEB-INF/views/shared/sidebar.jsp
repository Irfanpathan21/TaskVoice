<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<aside class="sidebar">
  <div class="brand">
    <div class="brand-icon">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/>
        <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
        <line x1="12" y1="19" x2="12" y2="22"/>
      </svg>
    </div>
    <span>TaskVoice</span>
  </div>

  <ul class="nav-list">
    <c:choose>
      <c:when test="${sessionScope.currentUser.roleName == 'ADMIN'}">
        <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link ${pageContext.request.requestURI.endsWith('dashboard') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/employees" class="nav-link ${pageContext.request.requestURI.contains('employees') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          Employees</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/departments" class="nav-link ${pageContext.request.requestURI.contains('departments') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
          Departments</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/categories" class="nav-link ${pageContext.request.requestURI.contains('categories') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="20" y2="12"/><line x1="4" y1="18" x2="20" y2="18"/></svg>
          Categories</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/audit-logs" class="nav-link ${pageContext.request.requestURI.contains('audit-logs') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
          Audit Logs</a></li>
      </c:when>

      <c:when test="${sessionScope.currentUser.roleName == 'MANAGER'}">
        <li><a href="${pageContext.request.contextPath}/manager/dashboard" class="nav-link ${pageContext.request.requestURI.contains('dashboard') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/projects" class="nav-link ${pageContext.request.requestURI.contains('projects') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          Projects</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/tasks" class="nav-link ${pageContext.request.requestURI.contains('tasks') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          Tasks</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/team" class="nav-link ${pageContext.request.requestURI.contains('team') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
          Team</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/timesheets" class="nav-link ${pageContext.request.requestURI.contains('timesheets') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          Timesheets</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/analytics" class="nav-link ${pageContext.request.requestURI.contains('analytics') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
          Analytics</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/appraisals" class="nav-link ${pageContext.request.requestURI.contains('appraisals') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          Appraisals</a></li>
        <li><a href="${pageContext.request.contextPath}/manager/reports" class="nav-link ${pageContext.request.requestURI.contains('reports') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>
          Reports</a></li>
      </c:when>

      <c:otherwise>
        <li><a href="${pageContext.request.contextPath}/employee/dashboard" class="nav-link ${pageContext.request.requestURI.contains('dashboard') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/voice-timesheet" class="nav-link ${pageContext.request.requestURI.contains('voice-timesheet') ? 'active' : ''}" style="color: #60A5FA; font-weight: 600;">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/></svg>
          Voice Timesheet</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/projects" class="nav-link ${pageContext.request.requestURI.contains('projects') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          My Projects</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/tasks" class="nav-link ${pageContext.request.requestURI.contains('tasks') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          My Tasks</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/calendar" class="nav-link ${pageContext.request.requestURI.contains('calendar') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          Calendar</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/appraisal" class="nav-link ${pageContext.request.requestURI.contains('appraisal') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
          My Appraisal</a></li>
        <li><a href="${pageContext.request.contextPath}/employee/reports" class="nav-link ${pageContext.request.requestURI.contains('reports') ? 'active' : ''}">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>
          Reports</a></li>
      </c:otherwise>
    </c:choose>
  </ul>

  <div class="user-profile-widget">
    <div class="avatar">${sessionScope.currentUser.name.substring(0,1)}</div>
    <div style="flex:1; overflow:hidden;">
      <div style="font-weight: 600; font-size: 13px; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">
        <c:out value="${sessionScope.currentUser.name}"/>
      </div>
      <div style="font-size: 11px; color: var(--text-muted);">
        <c:out value="${sessionScope.currentUser.roleName}"/>
      </div>
    </div>
    <a href="${pageContext.request.contextPath}/logout" style="color: var(--text-muted); display:flex;" title="Logout">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
    </a>
  </div>
</aside>
