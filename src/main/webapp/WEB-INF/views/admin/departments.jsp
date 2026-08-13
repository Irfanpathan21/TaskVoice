<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Departments — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl);">
        <div class="page-header">
          <div>
            <h1 class="page-title">Departments</h1>
            <p class="page-subtitle">Manage organizational structure</p>
          </div>
          <button onclick="document.getElementById('createDeptModal').style.display='flex'" class="btn btn-primary">
            + Add Department
          </button>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Department Name</th>
                <th>Description</th>
                <th>Headcount</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="d" items="${departments}">
                <tr>
                  <td class="mono"><c:out value="${d.id}"/></td>
                  <td style="font-weight: 600;"><c:out value="${d.name}"/></td>
                  <td style="color: var(--text-secondary);"><c:out value="${d.description}"/></td>
                  <td class="mono"><c:out value="${d.headcount}"/></td>
                  <td>
                    <form action="${pageContext.request.contextPath}/admin/departments" method="POST" style="display:inline;" onsubmit="return confirm('Delete department?');">
                      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                      <input type="hidden" name="action" value="delete"/>
                      <input type="hidden" name="id" value="${d.id}"/>
                      <button type="submit" class="btn btn-secondary" style="padding:4px 8px; font-size:11px;">Delete</button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div id="createDeptModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 440px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 20px;">Add Department</h2>
      <form action="${pageContext.request.contextPath}/admin/departments" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="create"/>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">NAME</label>
          <input type="text" name="name" class="form-control" required/>
        </div>

        <div style="margin-bottom: 24px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">DESCRIPTION</label>
          <textarea name="description" class="form-control" rows="3"></textarea>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('createDeptModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Save Department</button>
        </div>
      </form>
    </div>
  </div>
</body>
</html>
