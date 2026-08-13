<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Login — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <style>
    body {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
    }
    .login-card {
      width: 400px;
      padding: 40px;
    }
  </style>
</head>
  <div style="position: absolute; top: 20px; right: 20px;">
    <button id="themeToggleBtn" class="btn btn-secondary" onclick="toggleTheme()" style="padding: 8px 12px;" title="Toggle Theme">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
      </svg>
    </button>
  </div>
  <script src="${pageContext.request.contextPath}/js/theme.js"></script>

  <div class="glass-card login-card">
    <div style="text-align: center; margin-bottom: 32px;">
      <div class="brand-icon" style="width: 42px; height: 42px; margin: 0 auto 16px;">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="22"/></svg>
      </div>
      <h1 style="font-size: 24px; font-weight: 700;">TaskVoice</h1>
      <p style="color: var(--text-secondary); font-size: 13px; margin-top: 6px;">Sign in to your enterprise account</p>
    </div>

    <c:if test="${not empty error}">
      <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid rgba(244, 63, 94, 0.3); color: var(--accent-rose); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="POST">
      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
      
      <div style="margin-bottom: 20px;">
        <label style="display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px;">EMAIL ADDRESS</label>
        <input type="email" name="email" class="form-control" required placeholder="name@company.com" value="<c:out value='${email}'/>"/>
      </div>

      <div style="margin-bottom: 24px;">
        <label style="display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px;">PASSWORD</label>
        <input type="password" name="password" class="form-control" required placeholder="••••••••"/>
      </div>

      <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px; font-weight: 600;">Sign In</button>
    </form>
  </div>
</body>
</html>
