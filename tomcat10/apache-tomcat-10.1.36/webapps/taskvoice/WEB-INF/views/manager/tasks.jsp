<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Tasks — TaskVoice</title>
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
            <h1 class="page-title">Task Management & Evaluation</h1>
            <p class="page-subtitle">Assign tasks, track progress updates, and grade completed work</p>
          </div>
          <button onclick="document.getElementById('createTaskModal').style.display='flex'" class="btn btn-primary">
            + Create Task
          </button>
        </div>

        <c:if test="${not empty sessionScope.flashMessage}">
          <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-emerald); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${sessionScope.flashMessage}"/>
          </div>
          <c:remove var="flashMessage" scope="session"/>
        </c:if>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Emp No</th>
                <th>Employee Name</th>
                <th>Task Title</th>
                <th>Project</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Progress %</th>
                <th>Status</th>
                <th>Grade</th>
                <th>Remark</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="t" items="${tasks}">
                <tr>
                  <td class="mono"><c:out value="${t.assigneeNo}"/></td>
                  <td style="font-weight: 600;"><c:out value="${t.assigneeName}"/></td>
                  <td>
                    <a href="${pageContext.request.contextPath}/manager/tasks?id=${t.id}"><c:out value="${t.title}"/></a>
                  </td>
                  <td style="color: var(--text-secondary);"><c:out value="${t.projectTitle}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${t.startDate}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${t.dueDate}"/></td>
                  <td class="mono"><c:out value="${t.completionPct}"/>%</td>
                  <td><span class="status-pill status-${t.status.toLowerCase()}"><c:out value="${t.status}"/></span></td>
                  <td>
                    <c:choose>
                      <c:when test="${t.managerGrade != null}">
                        <span class="status-pill status-completed"><c:out value="${t.gradeDisplay}"/></span>
                      </c:when>
                      <c:otherwise><span style="font-size: 12px; color: var(--text-muted);">-</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td style="font-size: 12px; color: var(--text-secondary); max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                    <c:out value="${t.managerRemark != null ? t.managerRemark : '-'}"/>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div id="createTaskModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 520px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 20px;">Create & Assign Task</h2>
      <form action="${pageContext.request.contextPath}/manager/tasks" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="create"/>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">PROJECT</label>
          <select name="projectId" class="form-control" required>
            <c:forEach var="p" items="${projects}">
              <option value="${p.id}"><c:out value="${p.title}"/></option>
            </c:forEach>
          </select>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">TASK TITLE</label>
          <input type="text" name="title" class="form-control" required/>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">ASSIGNEE</label>
          <select name="assigneeId" class="form-control" required>
            <c:forEach var="m" items="${team}">
              <option value="${m.id}"><c:out value="${m.name}"/> (<c:out value="${m.employeeNo}"/></option>
            </c:forEach>
          </select>
        </div>

        <div style="display:flex; gap:16px; margin-bottom: 16px;">
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">CATEGORY</label>
            <select name="categoryId" class="form-control">
              <c:forEach var="cat" items="${categories}">
                <option value="${cat.id}"><c:out value="${cat.name}"/></option>
              </c:forEach>
            </select>
          </div>
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">PRIORITY</label>
            <select name="priority" class="form-control">
              <option value="LOW">LOW</option>
              <option value="MEDIUM" selected>MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="CRITICAL">CRITICAL</option>
            </select>
          </div>
        </div>

        <div style="display:flex; gap:16px; margin-bottom: 16px;">
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">START DATE</label>
            <input type="date" name="startDate" class="form-control" required/>
          </div>
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">DUE DATE</label>
            <input type="date" name="dueDate" class="form-control" required/>
          </div>
        </div>

        <div style="margin-bottom: 24px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">EXPECTED HOURS</label>
          <input type="number" step="0.5" name="expectedHours" class="form-control" required value="8.0"/>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('createTaskModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Create Task</button>
        </div>
      </form>
    </div>
  </div>
</body>
</html>
