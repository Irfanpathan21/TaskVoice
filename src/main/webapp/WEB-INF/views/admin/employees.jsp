<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Employees — TaskVoice</title>
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
            <h1 class="page-title">Employee Management</h1>
            <p class="page-subtitle">Create, assign managers, and manage organization accounts</p>
          </div>
          <button onclick="document.getElementById('createModal').style.display='flex'" class="btn btn-primary">
            + Create Account
          </button>
        </div>

        <c:if test="${not empty sessionScope.flashMessage}">
          <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-emerald); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${sessionScope.flashMessage}"/>
          </div>
          <c:remove var="flashMessage" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
          <div style="background: rgba(244, 63, 94, 0.15); border: 1px solid rgba(244, 63, 94, 0.3); color: var(--accent-rose); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${sessionScope.flashError}"/>
          </div>
          <c:remove var="flashError" scope="session"/>
        </c:if>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Role</th>
                <th>Assigned Manager</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="emp" items="${employees}">
                <tr>
                  <td class="mono"><c:out value="${emp.employeeNo}"/></td>
                  <td style="font-weight: 600;"><c:out value="${emp.name}"/></td>
                  <td style="color: var(--text-secondary);"><c:out value="${emp.email}"/></td>
                  <td><c:out value="${emp.departmentName != null ? emp.departmentName : '-'}"/></td>
                  <td><span class="status-pill status-in_progress"><c:out value="${emp.roleName}"/></span></td>
                  <td><c:out value="${emp.managerName != null ? emp.managerName : '-'}"/></td>
                  <td>
                    <span class="status-pill ${emp.status == 'ACTIVE' ? 'status-completed' : 'status-blocked'}">
                      <c:out value="${emp.status}"/>
                    </span>
                  </td>
                  <td>
                    <div style="display: flex; gap: 8px;">
                      <c:if test="${emp.status == 'ACTIVE'}">
                        <form action="${pageContext.request.contextPath}/admin/employees" method="POST" style="display:inline;">
                          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                          <input type="hidden" name="action" value="disable"/>
                          <input type="hidden" name="id" value="${emp.id}"/>
                          <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;">Disable</button>
                        </form>
                      </c:if>
                      <c:if test="${emp.status == 'DISABLED'}">
                        <form action="${pageContext.request.contextPath}/admin/employees" method="POST" style="display:inline;">
                          <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                          <input type="hidden" name="action" value="enable"/>
                          <input type="hidden" name="id" value="${emp.id}"/>
                          <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;">Enable</button>
                        </form>
                      </c:if>
                      <form action="${pageContext.request.contextPath}/admin/employees" method="POST" style="display:inline;" onsubmit="return confirm('Reset password for this user?');">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                        <input type="hidden" name="action" value="resetPassword"/>
                        <input type="hidden" name="id" value="${emp.id}"/>
                        <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 11px;">Reset PW</button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- Create Account Modal -->
  <div id="createModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 480px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 20px;">Create User Account</h2>
      <form action="${pageContext.request.contextPath}/admin/employees" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="create"/>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">FULL NAME</label>
          <input type="text" name="name" class="form-control" required placeholder="John Doe"/>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">EMAIL ADDRESS</label>
          <input type="email" name="email" class="form-control" required placeholder="john@company.com"/>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">ROLE</label>
          <select name="roleName" class="form-control" required>
            <option value="EMPLOYEE">EMPLOYEE</option>
            <option value="MANAGER">MANAGER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">DEPARTMENT</label>
          <select name="departmentId" class="form-control">
            <option value="">-- None --</option>
            <c:forEach var="d" items="${departments}">
              <option value="${d.id}"><c:out value="${d.name}"/></option>
            </c:forEach>
          </select>
        </div>

        <div style="margin-bottom: 24px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">ASSIGN MANAGER (if Employee)</label>
          <select name="managerId" class="form-control">
            <option value="">-- None --</option>
            <c:forEach var="m" items="${managers}">
              <option value="${m.id}"><c:out value="${m.name}"/></option>
            </c:forEach>
          </select>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('createModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Create Account</button>
        </div>
      </form>
    </div>
  </div>
</body>
</html>
