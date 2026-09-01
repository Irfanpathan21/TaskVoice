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
      int statusCode = 500;
      String errorTitle = "Internal Server Error";
      String errorDesc = "An unexpected error occurred while processing your request.";
      
      if (pageContext.getErrorData() != null) {
        statusCode = pageContext.getErrorData().getStatusCode();
      }
      
      if (statusCode == 403) {
        errorTitle = "Access Denied";
        errorDesc = "You do not have permission to view this resource.";
      } else if (statusCode == 404) {
        errorTitle = "Page Not Found";
        errorDesc = "The requested URL or resource does not exist.";
      } else if (statusCode == 500) {
        errorTitle = "Server Processing Error";
        errorDesc = "The server encountered an issue. Please try refreshing or logging back in.";
      }
      
      Throwable exceptionObj = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
      if (exceptionObj == null) {
        exceptionObj = exception;
      }
    %>
    <h1 style="font-size: 56px; color: #f43f5e; margin-bottom: 12px; font-weight: 800;">
      <%= statusCode %>
    </h1>
    <h2 style="font-size: 20px; margin-bottom: 12px; color: #f8fafc;"><%= errorTitle %></h2>
    <p style="color: #94a3b8; font-size: 14px; margin-bottom: 24px;">
      <%= errorDesc %>
    </p>

    <% if (exceptionObj != null) { %>
      <div style="text-align: left; background: rgba(0,0,0,0.4); padding: 12px; border-radius: 6px; font-family: monospace; font-size: 12px; color: #fb7185; margin-bottom: 24px; overflow-x: auto; max-height: 120px;">
        <%= exceptionObj.getMessage() != null ? exceptionObj.getMessage() : exceptionObj.toString() %>
      </div>
    <% } %>

    <div style="display: flex; gap: 12px; justify-content: center;">
      <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="padding: 10px 20px; border-radius: 6px; background: #3b82f6; color: #fff; text-decoration: none; font-weight: 600;">Return to Login</a>
    </div>
  </div>
</body>
</html>
