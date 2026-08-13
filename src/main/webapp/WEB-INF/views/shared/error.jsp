<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Error — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <style>
    body { display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; }
  </style>
</head>
<body>
  <div class="glass-card" style="max-width: 480px; padding: 40px;">
    <h1 style="font-size: 48px; color: var(--accent-rose); margin-bottom: 16px;">
      <%= pageContext.getErrorData() != null ? pageContext.getErrorData().getStatusCode() : "403" %>
    </h1>
    <h2 style="font-size: 20px; margin-bottom: 12px;">Access Denied or Page Not Found</h2>
    <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 24px;">
      You do not have permission to view this resource or the requested page does not exist.
    </p>
    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">Return to Safety</a>
  </div>
</body>
</html>
