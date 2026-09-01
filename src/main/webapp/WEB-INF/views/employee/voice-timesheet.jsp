<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Voice & Manual Timesheet Logger — TaskVoice</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages/voice-timesheet.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 960px; margin: 0 auto;">
        
        <!-- Voice & Manual Logger Hero Surface -->
        <div class="voice-hero">
          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
            <div>
              <h1 style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">Voice & Manual Timesheet Logger</h1>
              <p style="color: var(--text-secondary); font-size: 14px;">Speak naturally about your day or type your recap. AI will extract and pre-fill your task form automatically.</p>
            </div>
            <button type="button" class="btn btn-secondary" onclick="addBlankManualBlock()" style="display:flex; align-items:center; gap:6px;">
              <span>➕</span> Add Manual Timesheet Entry
            </button>
          </div>

          <div class="mic-button-wrapper" style="margin-top: 20px;">
            <button id="micBtn" class="mic-button" title="Click to Record Voice">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="22"/>
              </svg>
            </button>
          </div>

          <div id="statusText" style="font-weight: 500; font-size: 14px; color: var(--text-primary); text-align: center; margin-top: 12px;">
            Press the mic button to record your daily work recap, or type your update below.
          </div>

          <div id="stageLabel" class="stage-label" style="text-align: center; margin-top: 6px; font-size: 13px; color: var(--accent-blue);"></div>

          <!-- Live Transcript & Manual Typed Input Box -->
          <div id="transcriptBox" class="live-transcript-box" contenteditable="true" style="min-height: 80px; outline: none; border: 1px solid var(--border); padding: 12px; border-radius: var(--radius-md); background: rgba(0,0,0,0.1); margin-top: 16px;" placeholder="Type your work recap here if you don't have a microphone..."></div>
          
          <div style="margin-top: 12px; display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 12px; color: var(--text-muted);">💡 Tip: You can edit or add task fields manually before saving.</span>
            <button id="processTextBtn" class="btn btn-secondary">Process Typed Text</button>
          </div>
        </div>

        <!-- Generated & Manual Work Blocks Form Section -->
        <div id="reviewContainer" style="display: none; margin-top: 28px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px;">
            <div>
              <h2 style="font-size: 18px; font-weight: 700;">Editable Timesheet Form Entries</h2>
              <span id="totalHoursBadge" style="font-size: 13px; color: var(--accent-emerald); font-weight: 600;">Total Logged: 0.0 Hours</span>
            </div>
            <div style="display:flex; gap:10px;">
              <button type="button" class="btn btn-secondary" onclick="addBlankManualBlock()">➕ Add Another Entry</button>
              <button id="confirmSaveBtn" class="btn btn-primary">Confirm & Save All Entries</button>
            </div>
          </div>

          <!-- Form Grid for Work Cards -->
          <div id="workBlocksGrid" class="work-blocks-grid"></div>
        </div>

      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/voice-recorder.js?v=12"></script>
  <script>
    window.onerror = function(msg, url, lineNo, columnNo, error) {
      console.error('JS Error:', msg, 'at line', lineNo);
      const el = document.getElementById('statusText');
      if (el) el.textContent = '⚠️ JS Error: ' + msg + ' (line ' + lineNo + ')';
      return false;
    };

    let currentRecordId = null;
    let generatedBlocks = [];

    // Pre-loaded DB Category & Task lists for select dropdowns
    const availableCategories = [
      <c:forEach items="${categories}" var="c" varStatus="st">
        { id: <c:out value="${c.id}"/>, name: "<c:out value="${c.name}"/>" }<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];

    const availableTasks = [
      <c:forEach items="${assignedTasks}" var="t" varStatus="st">
        { id: <c:out value="${t.id}"/>, title: "<c:out value="${t.title}"/>" }<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];

    const recorder = new VoiceRecorder({
      micBtnId: 'micBtn',
      statusTextId: 'statusText',
      transcriptBoxId: 'transcriptBox',
      stageLabelId: 'stageLabel',
      csrfToken: '${sessionScope.csrfToken}',
      onBlocksReceived: (recordId, blocks, fullTranscript) => {
        currentRecordId = recordId;
        generatedBlocks = blocks || [];
        renderWorkBlocks(generatedBlocks, fullTranscript);
      }
    });

    function renderWorkBlocks(blocks, fullTranscript) {
      const container = document.getElementById('reviewContainer');
      const grid = document.getElementById('workBlocksGrid');
      grid.innerHTML = '';
      container.style.display = 'block';

      let totalHours = 0.0;

      blocks.forEach((b, index) => {
        const hrs = parseFloat(b.durationHours) || 0.0;
        totalHours += hrs;

        const card = createWorkBlockCard(b, index, fullTranscript);
        grid.appendChild(card);
      });

      updateTotalHoursDisplay(totalHours);
    }

    function createWorkBlockCard(b, index, fullTranscript) {
      const card = document.createElement('div');
      card.className = 'work-block-card';
      card.dataset.index = index;

      // Category options dropdown
      let catOptionsHtml = '<option value="">-- Select Category --</option>';
      const catList = availableCategories.length > 0 ? availableCategories : [
        {id: 1, name: 'Development'}, {id: 2, name: 'Design'}, {id: 3, name: 'Meetings'}, {id: 4, name: 'Testing'}
      ];
      catList.forEach(c => {
        const selected = (b.categoryId && parseInt(b.categoryId) === parseInt(c.id)) ||
                         (b.category && b.category.toLowerCase() === c.name.toLowerCase()) ? 'selected' : '';
        catOptionsHtml += `<option value="${c.id}" ${selected}>${escapeHtml(c.name)}</option>`;
      });

      // Linked Task options dropdown
      let taskOptionsHtml = '<option value="">-- No Linked Task --</option>';
      availableTasks.forEach(t => {
        const selected = (b.matchedTaskId && parseInt(b.matchedTaskId) === parseInt(t.id)) ||
                         (b.matchedTaskTitle && b.matchedTaskTitle.toLowerCase() === t.title.toLowerCase()) ? 'selected' : '';
        taskOptionsHtml += `<option value="${t.id}" ${selected}>${escapeHtml(t.title)}</option>`;
      });

      const spokenPhraseTag = fullTranscript ? `<span class="original-phrase-tag">Original Input: "${escapeHtml(fullTranscript)}"</span>` : '';

      card.innerHTML = `
        ${spokenPhraseTag}
        <div style="margin-bottom: 12px;">
          <label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px; font-weight:600;">TASK / ACTIVITY TITLE</label>
          <input type="text" class="form-control block-title" value="${escapeHtml(b.title || '')}" placeholder="e.g. Implemented User Auth Servlet"/>
        </div>
        <div style="display:flex; gap:12px; margin-bottom:12px; flex-wrap:wrap;">
          <div style="flex:1; min-width: 140px;">
            <label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px; font-weight:600;">WORK CATEGORY</label>
            <select class="form-control block-category-id">${catOptionsHtml}</select>
          </div>
          <div style="flex:1; min-width: 160px;">
            <label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px; font-weight:600;">LINKED ASSIGNED TASK</label>
            <select class="form-control block-task-id">${taskOptionsHtml}</select>
          </div>
          <div style="width:110px;">
            <label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px; font-weight:600;">HOURS LOGGED</label>
            <input type="number" step="0.5" min="0.25" max="24" class="form-control block-duration" value="${b.durationHours || 1.0}" onchange="recalculateTotalHours()"/>
          </div>
        </div>
        <div style="margin-bottom: 12px;">
          <label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px; font-weight:600;">WORK DESCRIPTION / NOTES</label>
          <textarea class="form-control block-desc" rows="2" placeholder="Summary of work performed">${escapeHtml(b.description || '')}</textarea>
        </div>
        <div style="text-align: right;">
          <button type="button" class="btn btn-secondary" onclick="removeWorkBlock(this)" style="font-size:11px; color:var(--accent-rose);">Remove Entry</button>
        </div>
      `;

      return card;
    }

    function addBlankManualBlock() {
      const container = document.getElementById('reviewContainer');
      const grid = document.getElementById('workBlocksGrid');
      container.style.display = 'block';

      const newBlock = {
        title: '',
        category: 'Development',
        durationHours: 1.0,
        description: ''
      };

      const card = createWorkBlockCard(newBlock, generatedBlocks.length, '');
      grid.appendChild(card);
      recalculateTotalHours();

      card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function removeWorkBlock(button) {
      const card = button.closest('.work-block-card');
      if (card) {
        card.remove();
        recalculateTotalHours();
      }
    }

    function recalculateTotalHours() {
      let total = 0.0;
      document.querySelectorAll('.block-duration').forEach(input => {
        total += parseFloat(input.value) || 0.0;
      });
      updateTotalHoursDisplay(total);
    }

    function updateTotalHoursDisplay(total) {
      const badge = document.getElementById('totalHoursBadge');
      if (badge) badge.textContent = `Total Logged: ${total.toFixed(1)} Hours`;
    }

    document.getElementById('confirmSaveBtn').addEventListener('click', async () => {
      const cards = document.querySelectorAll('.work-block-card');
      if (cards.length === 0) {
        alert('Please add at least one timesheet entry before saving.');
        return;
      }

      const finalBlocks = [];
      cards.forEach(card => {
        const catSelect = card.querySelector('.block-category-id');
        const taskSelect = card.querySelector('.block-task-id');
        
        finalBlocks.push({
          title: card.querySelector('.block-title').value || 'Work Entry',
          categoryId: catSelect.value ? parseInt(catSelect.value) : null,
          category: catSelect.options[catSelect.selectedIndex] ? catSelect.options[catSelect.selectedIndex].text : '',
          matchedTaskId: taskSelect.value ? parseInt(taskSelect.value) : null,
          durationHours: parseFloat(card.querySelector('.block-duration').value) || 1.0,
          description: card.querySelector('.block-desc').value || ''
        });
      });

      const formData = new URLSearchParams();
      formData.append('action', 'confirm');
      formData.append('recordId', currentRecordId || '0');
      formData.append('blocksJson', JSON.stringify(finalBlocks));
      formData.append('_csrf', '${sessionScope.csrfToken}');

      const resp = await fetch('voice-timesheet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
      });

      const data = await resp.json();
      if (data.status === 'ok') {
        window.location.href = '${pageContext.request.contextPath}/employee/calendar';
      }
    });

    function escapeHtml(str) {
      return str ? String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;") : '';
    }
  </script>
</body>
</html>
