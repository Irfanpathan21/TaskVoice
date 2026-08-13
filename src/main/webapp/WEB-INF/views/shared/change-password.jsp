<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Security Settings — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>
      <div style="padding: var(--space-xl); max-width: 600px;">
        <h1 class="page-title" style="margin-bottom: 24px;">Security Settings</h1>
        
        <c:if test="${not empty error}">
          <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid rgba(244, 63, 94, 0.3); color: var(--accent-rose); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${error}"/>
          </div>
        </c:if>

        <div class="glass-card">
          <form action="${pageContext.request.contextPath}/shared/change-password" method="POST">
            <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
            
            <c:if test="${!sessionScope.currentUser.forcePwChange}">
              <div style="margin-bottom: 20px;">
                <label style="display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px;">CURRENT PASSWORD</label>
                <input type="password" name="currentPassword" class="form-control" required/>
              </div>
            </c:if>

            <div style="margin-bottom: 20px;">
              <label style="display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px;">NEW PASSWORD (min 8 chars)</label>
              <input type="password" name="newPassword" class="form-control" required minlength="8"/>
            </div>

            <div style="margin-bottom: 24px;">
              <label style="display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 8px;">CONFIRM NEW PASSWORD</label>
              <input type="password" name="confirmPassword" class="form-control" required minlength="8"/>
            </div>

            <button type="submit" class="btn btn-primary">Update Password</button>
          </form>
        </div>
      </div>
    </div>
  </div>
</body>
</html>
