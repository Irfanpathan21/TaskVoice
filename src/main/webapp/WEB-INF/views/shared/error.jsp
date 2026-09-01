<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Error — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <style>
    body { display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; background: #0f172a; color: #f8fafc; }
  </style>
</head>
<body>
  <div class="glass-card" style="max-width: 520px; padding: 40px; border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; background: rgba(30, 41, 59, 0.8);">
    <%
      Object statusObj = request.getAttribute("jakarta.servlet.error.status_code");
      Object messageObj = request.getAttribute("jakarta.servlet.error.message");
      Object exceptionObj = request.getAttribute("jakarta.servlet.error.exception");
      if (exceptionObj == null) exceptionObj = exception;

      int statusCode = statusObj != null ? Integer.parseInt(statusObj.toString()) : 500;
      String errorMessage = messageObj != null ? messageObj.toString() : "";
      if (exceptionObj != null && errorMessage.isBlank()) {
        errorMessage = ((Throwable)exceptionObj).getMessage();
      }
    %>
    <h1 style="font-size: 56px; color: #f43f5e; margin-bottom: 12px; font-weight: 800;">
      <%= statusCode %>
    </h1>
    <h2 style="font-size: 20px; margin-bottom: 12px; color: #f8fafc;">Server Processing Error</h2>
    <p style="color: #94a3b8; font-size: 14px; margin-bottom: 24px;">
      An issue occurred while processing your request.
    </p>

    <% if (errorMessage != null && !errorMessage.isBlank()) { %>
      <div style="text-align: left; background: rgba(244, 63, 94, 0.1); border: 1px solid rgba(244, 63, 94, 0.3); padding: 12px; border-radius: 6px; font-family: monospace; font-size: 13px; color: #fb7185; margin-bottom: 24px;">
        <strong>Details:</strong> <%= errorMessage %>
      </div>
    <% } %>

    <div style="display: flex; gap: 12px; justify-content: center;">
      <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="padding: 10px 20px; border-radius: 6px; background: #3b82f6; color: #fff; text-decoration: none; font-weight: 600;">Return to Login</a>
    </div>
  </div>
</body>
</html>
