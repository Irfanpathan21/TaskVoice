<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<header class="top-navbar">
  <div style="display: flex; align-items: center; gap: 16px;">
    <span style="font-weight: 500; color: var(--text-secondary);">
      Welcome back, <strong style="color: var(--text-primary);"><c:out value="${sessionScope.currentUser.name}"/></strong>
    </span>
  </div>

  <div style="display: flex; align-items: center; gap: 16px;">
    <button id="themeToggleBtn" class="btn btn-secondary" onclick="toggleTheme()" style="padding: 8px 12px;" title="Toggle Theme">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </button>

    <button id="notifBellBtn" class="btn btn-secondary" style="position: relative; padding: 8px 12px;">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <span id="notifBadge" class="status-pill status-overdue" style="display: none; position: absolute; top: -4px; right: -4px; padding: 2px 6px; font-size: 10px;">0</span>
    </button>

    <a href="${pageContext.request.contextPath}/shared/change-password" class="btn btn-secondary" style="font-size: 12px;">
      Security
    </a>
  </div>
</header>
<script src="${pageContext.request.contextPath}/js/theme.js"></script>

<!-- Notifications Drawer Panel -->
<div id="notifPanel" class="glass" style="position: fixed; top: 0; right: -360px; width: 360px; height: 100vh; z-index: 1000; transition: right 300ms cubic-bezier(0.16,1,0.3,1); display: flex; flex-direction: column;">
  <div style="padding: 20px; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center;">
    <h3 style="font-size: 16px;">Notifications</h3>
    <button id="notifCloseBtn" style="background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: 18px;">&times;</button>
  </div>
  <div id="notifList" style="flex: 1; overflow-y: auto; padding: 12px;"></div>
  <div style="padding: 16px; border-top: 1px solid var(--border); text-align: center;">
    <button id="markAllReadBtn" class="btn btn-secondary" style="width: 100%; font-size: 12px;">Mark All as Read</button>
  </div>
</div>

<script>
  window.contextPath = '${pageContext.request.contextPath}';
  window.csrfToken   = '${sessionScope.csrfToken}';
</script>
<script src="${pageContext.request.contextPath}/js/notifications.js"></script>
