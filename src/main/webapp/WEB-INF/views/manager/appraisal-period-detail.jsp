<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><c:out value="${period.title}"/> — Team Appraisals</title>
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
            <h1 class="page-title"><c:out value="${period.title}"/></h1>
            <p class="page-subtitle">Period: <span class="mono"><c:out value="${period.startDate}"/> → <c:out value="${period.endDate}"/></span> | Type: <c:out value="${period.periodType}"/></p>
          </div>
          <a href="${pageContext.request.contextPath}/manager/appraisals" class="btn btn-secondary">&larr; Back to Periods</a>
        </div>

        <c:if test="${not empty sessionScope.flashMessage}">
          <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: var(--accent-emerald); padding: 12px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: 20px;">
            <c:out value="${sessionScope.flashMessage}"/>
          </div>
          <c:remove var="flashMessage" scope="session"/>
        </c:if>

        <h3 style="font-size: 16px; margin-bottom: 16px;">Team Evaluation Cards</h3>

        <!-- Grid of Team Members for Appraisal -->
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 20px;">
          <c:forEach var="member" items="${team}">
            <c:set var="userAppraisal" value="${null}"/>
            <c:forEach var="a" items="${appraisals}">
              <c:if test="${a.employeeId == member.id}">
                <c:set var="userAppraisal" value="${a}"/>
              </c:if>
            </c:forEach>

            <div class="glass-card" style="display:flex; flex-direction:column; justify-content:space-between;">
              <div>
                <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom: 12px;">
                  <div>
                    <h4 style="font-size: 16px; font-weight: 700;"><c:out value="${member.name}"/></h4>
                    <div class="mono" style="font-size: 11px; color: var(--text-muted);"><c:out value="${member.employeeNo}"/></div>
                  </div>
                  <c:choose>
                    <c:when test="${userAppraisal != null && userAppraisal.finalStatus == 'FINALIZED'}">
                      <span class="status-pill status-completed">FINALIZED</span>
                    </c:when>
                    <c:when test="${userAppraisal != null}">
                      <span class="status-pill status-under_review">NEEDS REVIEW</span>
                    </c:when>
                    <c:otherwise>
                      <span class="status-pill status-in_progress">NOT STARTED</span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <div style="font-size: 13px; color: var(--text-secondary); margin-bottom: 16px;">
                  <div><strong>Email:</strong> <c:out value="${member.email}"/></div>
                  <div><strong>Dept:</strong> <c:out value="${member.departmentName != null ? member.departmentName : 'Engineering'}"/></div>
                </div>

                <c:if test="${userAppraisal != null}">
                  <div style="padding: 12px; background: rgba(255,255,255,0.03); border: 1px solid var(--border); border-radius: var(--radius-sm); margin-bottom: 16px;">
                    <div style="display:flex; justify-content:space-between; font-size: 12px; margin-bottom: 4px;">
                      <span>AI Score: <strong><c:out value="${userAppraisal.aiScore != null ? userAppraisal.aiScore : '-'}"/></strong></span>
                      <span>AI Grade: <strong><c:out value="${userAppraisal.aiGradeDisplay != null ? userAppraisal.aiGradeDisplay : '-'}"/></strong></span>
                    </div>
                    <c:if test="${userAppraisal.finalStatus == 'FINALIZED'}">
                      <div style="font-size: 12px; color: var(--accent-emerald); margin-top: 4px;">
                        Final Manager Score: <strong><c:out value="${userAppraisal.managerScore}"/></strong> (<c:out value="${userAppraisal.managerGradeDisplay}"/>)
                      </div>
                    </c:if>
                  </div>
                </c:if>
              </div>

              <div>
                <c:choose>
                  <c:when test="${userAppraisal == null}">
                    <form action="${pageContext.request.contextPath}/manager/appraisals" method="POST">
                      <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}"/>
                      <input type="hidden" name="action" value="triggerAi"/>
                      <input type="hidden" name="periodId" value="${period.id}"/>
                      <input type="hidden" name="employeeId" value="${member.id}"/>
                      <button type="submit" class="btn btn-primary" style="width: 100%;">🤖 Run AI Appraisal</button>
                    </form>
                  </c:when>
                  <c:otherwise>
                    <a href="${pageContext.request.contextPath}/manager/appraisals?id=${userAppraisal.id}" class="btn btn-secondary" style="width: 100%; text-align: center;">
                      ${userAppraisal.finalStatus == 'FINALIZED' ? '✅ View Final Appraisal' : '✏️ Review & Finalize'}
                    </a>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>

      </div>
    </div>
  </div>
</body>
</html>
