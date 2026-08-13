<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Projects — TaskVoice</title>
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
            <h1 class="page-title">Project Management</h1>
            <p class="page-subtitle">Track project timelines, assignments, and AI completion sentiment</p>
          </div>
          <button onclick="document.getElementById('createProjectModal').style.display='flex'" class="btn btn-primary">
            + New Project
          </button>
        </div>

        <div class="table-container glass">
          <table class="enterprise-table">
            <thead>
              <tr>
                <th>Project Title</th>
                <th>Status</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Progress %</th>
                <th>AI Sentiment</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="p" items="${projects}">
                <tr>
                  <td style="font-weight: 600;">
                    <a href="${pageContext.request.contextPath}/manager/projects?id=${p.id}"><c:out value="${p.title}"/></a>
                  </td>
                  <td><span class="status-pill status-${p.status.toLowerCase()}"><c:out value="${p.status}"/></span></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${p.startDate}"/></td>
                  <td class="mono" style="font-size: 12px;"><c:out value="${p.endDate}"/></td>
                  <td class="mono"><c:out value="${String.format('%.0f', p.progressPct)}"/>%</td>
                  <td>
                    <c:choose>
                      <c:when test="${p.hasSentiment()}">
                        <span class="status-pill ${p.aiSentiment == 'POSITIVE' ? 'status-completed' : (p.aiSentiment == 'NEGATIVE' ? 'status-blocked' : 'status-under_review')}">
                          <c:out value="${p.aiSentiment}"/>
                        </span>
                      </c:when>
                      <c:otherwise><span style="font-size: 12px; color: var(--text-muted);">-</span></c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <c:if test="${p.status != 'COMPLETED'}">
                      <form action="${pageContext.request.contextPath}/manager/projects" method="POST" style="display:inline;">
                        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                        <input type="hidden" name="action" value="complete"/>
                        <input type="hidden" name="id" value="${p.id}"/>
                        <button type="submit" class="btn btn-secondary" style="padding:4px 8px; font-size:11px;">Complete & Run AI</button>
                      </form>
                    </c:if>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div id="createProjectModal" style="display:none; position:fixed; top:0; left:0; width:100vw; height:100vh; background:rgba(0,0,0,0.7); backdrop-filter:blur(10px); z-index:2000; align-items:center; justify-content:center;">
    <div class="glass-card" style="width: 500px; padding: 32px;">
      <h2 style="font-size: 20px; margin-bottom: 20px;">Create Project</h2>
      <form action="${pageContext.request.contextPath}/manager/projects" method="POST">
        <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
        <input type="hidden" name="action" value="create"/>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">PROJECT TITLE</label>
          <input type="text" name="title" class="form-control" required/>
        </div>

        <div style="margin-bottom: 16px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">DESCRIPTION</label>
          <textarea name="description" class="form-control" rows="3"></textarea>
        </div>

        <div style="display:flex; gap:16px; margin-bottom: 16px;">
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">START DATE</label>
            <input type="date" name="startDate" class="form-control" required/>
          </div>
          <div style="flex:1;">
            <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">END DATE</label>
            <input type="date" name="endDate" class="form-control" required/>
          </div>
        </div>

        <div style="margin-bottom: 24px;">
          <label style="display:block; font-size:12px; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">ASSIGN TEAM MEMBERS</label>
          <select name="memberIds" class="form-control" multiple style="height: 100px;">
            <c:forEach var="m" items="${team}">
              <option value="${m.id}"><c:out value="${m.name}"/> (<c:out value="${m.employeeNo}"/>)</option>
            </c:forEach>
          </select>
        </div>

        <div style="display:flex; justify-content:flex-end; gap:12px;">
          <button type="button" onclick="document.getElementById('createProjectModal').style.display='none'" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary">Create Project</button>
        </div>
      </form>
    </div>
  </div>
</body>
</html>
