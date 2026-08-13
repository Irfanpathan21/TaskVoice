document.addEventListener('DOMContentLoaded', () => {
  const bellBtn = document.getElementById('notifBellBtn');
  const badge = document.getElementById('notifBadge');
  const panel = document.getElementById('notifPanel');
  const closeBtn = document.getElementById('notifCloseBtn');
  const listContainer = document.getElementById('notifList');
  const markAllBtn = document.getElementById('markAllReadBtn');

  if (!bellBtn) return;

  // Fetch unread count
  async function updateUnreadCount() {
    try {
      const res = await fetch(window.contextPath + '/shared/notifications?action=count');
      const data = await res.json();
      if (data.count > 0) {
        badge.textContent = data.count;
        badge.style.display = 'inline-flex';
      } else {
        badge.style.display = 'none';
      }
    } catch (e) {
      console.warn('Failed to fetch notification count:', e);
    }
  }

  // Load notification list
  async function loadNotifications() {
    try {
      const res = await fetch(window.contextPath + '/shared/notifications');
      const notifs = await res.json();

      if (!notifs || notifs.length === 0) {
        listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: var(--text-muted);">No notifications yet.</div>';
        return;
      }

      listContainer.innerHTML = notifs.map(n => `
        <div class="notif-item ${n.read ? 'read' : 'unread'}" data-id="${n.id}">
          <div style="font-weight: 600; font-size: 13px;">${escapeHtml(n.title)}</div>
          <div style="color: var(--text-secondary); font-size: 12px; margin-top: 4px;">${escapeHtml(n.message)}</div>
          <div style="color: var(--text-muted); font-size: 10px; margin-top: 6px;">${new Date(n.createdAt).toLocaleString()}</div>
        </div>
      `).join('');
    } catch (e) {
      console.warn('Failed to load notifications:', e);
    }
  }

  bellBtn.addEventListener('click', () => {
    panel.classList.toggle('open');
    if (panel.classList.contains('open')) {
      loadNotifications();
    }
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', () => panel.classList.remove('open'));
  }

  if (markAllBtn) {
    markAllBtn.addEventListener('click', async () => {
      const formData = new URLSearchParams();
      formData.append('action', 'markAllRead');
      formData.append('_csrf', window.csrfToken);
      await fetch(window.contextPath + '/shared/notifications', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
      });
      updateUnreadCount();
      loadNotifications();
    });
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  updateUnreadCount();
  setInterval(updateUnreadCount, 60000); // Check every minute
});
