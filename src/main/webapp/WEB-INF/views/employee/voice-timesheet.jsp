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

        <%-- ═══════ Voice & Manual Logger Hero ═══════ --%>
        <div class="voice-hero">
          <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
            <div>
              <h1 style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">Voice & Manual Timesheet Logger</h1>
              <p style="color: var(--text-secondary); font-size: 14px;">
                Speak naturally about your day or type your recap. AI will extract and pre-fill your task form automatically.
              </p>
            </div>
            <button type="button" class="btn btn-secondary" id="addManualTopBtn" style="display:flex; align-items:center; gap:6px;">
              <span>&#10133;</span> Add Manual Timesheet Entry
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

          <div id="stageLabel" class="stage-label" style="text-align: center; margin-top: 6px;"></div>

          <%-- Live Transcript & Manual Typed Input Box --%>
          <div id="transcriptBox" class="live-transcript-box" contenteditable="true"
               style="min-height: 80px; outline: none; border: 1px solid var(--border); padding: 12px; border-radius: var(--radius-md); background: rgba(0,0,0,0.1); margin-top: 16px;">
          </div>

          <div style="margin-top: 12px; display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 12px; color: var(--text-muted);">&#128161; Tip: You can edit or add task fields manually before saving.</span>
            <button id="processTextBtn" class="btn btn-secondary">Process Typed Text</button>
          </div>
        </div>

        <%-- ═══════ Editable Work Block Form Section ═══════ --%>
        <div id="reviewContainer" style="display: none; margin-top: 28px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px;">
            <div>
              <h2 style="font-size: 18px; font-weight: 700;">Editable Timesheet Form Entries</h2>
              <span id="totalHoursBadge" style="font-size: 13px; color: var(--accent-emerald); font-weight: 600;">Total Logged: 0.0 Hours</span>
            </div>
            <div style="display:flex; gap:10px;">
              <button type="button" class="btn btn-secondary" id="addManualBottomBtn">&#10133; Add Another Entry</button>
              <button id="confirmSaveBtn" class="btn btn-primary">Confirm & Save All Entries</button>
            </div>
          </div>
          <div id="workBlocksGrid" class="work-blocks-grid"></div>
        </div>

      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/voice-recorder.js?v=20"></script>
  <script>
  // ═══════════════════════════════════════════════════════════
  //  VOICE TIMESHEET CONTROLLER — NO JSP EL INSIDE JS STRINGS
  // ═══════════════════════════════════════════════════════════
  (function() {
    'use strict';

    // ── Utility: HTML-escape (pure JS, no EL conflict) ──
    function esc(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    // ── Pre-loaded DB data (injected by JSP c:forEach) ──
    var availableCategories = [
      <c:forEach items="${categories}" var="cat" varStatus="st">
        { id: <c:out value="${cat.id}"/>, name: "<c:out value="${cat.name}"/>" }<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];
    if (availableCategories.length === 0) {
      availableCategories = [
        { id: 1, name: 'Development' }, { id: 2, name: 'Design' },
        { id: 3, name: 'Meetings' }, { id: 4, name: 'Testing' },
        { id: 5, name: 'Other' }
      ];
    }

    var availableTasks = [
      <c:forEach items="${assignedTasks}" var="task" varStatus="st">
        { id: <c:out value="${task.id}"/>, title: "<c:out value="${task.title}"/>" }<c:if test="${!st.last}">,</c:if>
      </c:forEach>
    ];

    var csrfToken = '<c:out value="${sessionScope.csrfToken}"/>';
    var contextPath = '<c:out value="${pageContext.request.contextPath}"/>';

    var currentRecordId = null;

    // ── Voice Recorder Init ──
    var recorder = new VoiceRecorder({
      micBtnId: 'micBtn',
      statusTextId: 'statusText',
      transcriptBoxId: 'transcriptBox',
      stageLabelId: 'stageLabel',
      csrfToken: csrfToken,
      onBlocksReceived: function(recordId, blocks, fullTranscript) {
        currentRecordId = recordId;
        renderWorkBlocks(blocks || [], fullTranscript || '');
      }
    });

    // ── Render AI-generated work blocks into editable form cards ──
    function renderWorkBlocks(blocks, fullTranscript) {
      var container = document.getElementById('reviewContainer');
      var grid = document.getElementById('workBlocksGrid');
      grid.innerHTML = '';
      container.style.display = 'block';

      var totalHours = 0;
      for (var i = 0; i < blocks.length; i++) {
        var hrs = parseFloat(blocks[i].durationHours) || 0;
        totalHours += hrs;
        grid.appendChild(createCard(blocks[i], fullTranscript));
      }
      updateTotalHours();
    }

    // ── Create a single editable form card ──
    function createCard(block, transcript) {
      var card = document.createElement('div');
      card.className = 'work-block-card';

      // Build category <select> options
      var catOpts = '<option value="">-- Select Category --</option>';
      for (var i = 0; i < availableCategories.length; i++) {
        var c = availableCategories[i];
        var sel = '';
        if (block.categoryId && parseInt(block.categoryId) === c.id) sel = ' selected';
        else if (block.category && block.category.toLowerCase() === c.name.toLowerCase()) sel = ' selected';
        catOpts += '<option value="' + c.id + '"' + sel + '>' + esc(c.name) + '</option>';
      }

      // Build task <select> options
      var taskOpts = '<option value="">-- No Linked Task --</option>';
      for (var j = 0; j < availableTasks.length; j++) {
        var t = availableTasks[j];
        var tsel = '';
        if (block.matchedTaskId && parseInt(block.matchedTaskId) === t.id) tsel = ' selected';
        taskOpts += '<option value="' + t.id + '"' + tsel + '>' + esc(t.title) + '</option>';
      }

      var transcriptTag = '';
      if (transcript) {
        transcriptTag = '<span class="original-phrase-tag">Original: "' + esc(transcript) + '"</span>';
      }

      card.innerHTML = transcriptTag +
        '<div style="margin-bottom:12px;">' +
          '<label style="display:block;font-size:11px;color:var(--text-secondary);margin-bottom:4px;font-weight:600;">TASK / ACTIVITY TITLE</label>' +
          '<input type="text" class="form-control block-title" value="' + esc(block.title || '') + '" placeholder="e.g. Implemented User Auth Servlet"/>' +
        '</div>' +
        '<div style="display:flex;gap:12px;margin-bottom:12px;flex-wrap:wrap;">' +
          '<div style="flex:1;min-width:140px;">' +
            '<label style="display:block;font-size:11px;color:var(--text-secondary);margin-bottom:4px;font-weight:600;">WORK CATEGORY</label>' +
            '<select class="form-control block-category-id">' + catOpts + '</select>' +
          '</div>' +
          '<div style="flex:1;min-width:160px;">' +
            '<label style="display:block;font-size:11px;color:var(--text-secondary);margin-bottom:4px;font-weight:600;">LINKED ASSIGNED TASK</label>' +
            '<select class="form-control block-task-id">' + taskOpts + '</select>' +
          '</div>' +
          '<div style="width:110px;">' +
            '<label style="display:block;font-size:11px;color:var(--text-secondary);margin-bottom:4px;font-weight:600;">HOURS LOGGED</label>' +
            '<input type="number" step="0.5" min="0.25" max="24" class="form-control block-duration" value="' + (block.durationHours || 1.0) + '"/>' +
          '</div>' +
        '</div>' +
        '<div style="margin-bottom:12px;">' +
          '<label style="display:block;font-size:11px;color:var(--text-secondary);margin-bottom:4px;font-weight:600;">WORK DESCRIPTION / NOTES</label>' +
          '<textarea class="form-control block-desc" rows="2" placeholder="Summary of work performed">' + esc(block.description || '') + '</textarea>' +
        '</div>' +
        '<div style="text-align:right;">' +
          '<button type="button" class="btn btn-secondary remove-entry-btn" style="font-size:11px;color:var(--accent-rose);">Remove Entry</button>' +
        '</div>';

      // Wire duration change listener
      var durInput = card.querySelector('.block-duration');
      if (durInput) durInput.addEventListener('change', updateTotalHours);

      // Wire remove button
      var removeBtn = card.querySelector('.remove-entry-btn');
      if (removeBtn) {
        removeBtn.addEventListener('click', function() {
          card.remove();
          updateTotalHours();
        });
      }

      return card;
    }

    // ── Add blank manual entry ──
    function addBlankManualBlock() {
      var container = document.getElementById('reviewContainer');
      var grid = document.getElementById('workBlocksGrid');
      container.style.display = 'block';

      var blank = { title: '', category: '', durationHours: 1.0, description: '' };
      var card = createCard(blank, '');
      grid.appendChild(card);
      updateTotalHours();
      card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    // ── Recalculate & display total hours ──
    function updateTotalHours() {
      var total = 0;
      var inputs = document.querySelectorAll('.block-duration');
      for (var i = 0; i < inputs.length; i++) {
        total += parseFloat(inputs[i].value) || 0;
      }
      var badge = document.getElementById('totalHoursBadge');
      if (badge) badge.textContent = 'Total Logged: ' + total.toFixed(1) + ' Hours';
    }

    // ── Confirm & Save All Entries ──
    document.getElementById('confirmSaveBtn').addEventListener('click', function() {
      var cards = document.querySelectorAll('.work-block-card');
      if (cards.length === 0) {
        alert('Please add at least one timesheet entry before saving.');
        return;
      }

      var finalBlocks = [];
      for (var i = 0; i < cards.length; i++) {
        var card = cards[i];
        var catSelect = card.querySelector('.block-category-id');
        var taskSelect = card.querySelector('.block-task-id');

        finalBlocks.push({
          title: card.querySelector('.block-title').value || 'Work Entry',
          categoryId: catSelect.value ? parseInt(catSelect.value) : null,
          category: catSelect.options[catSelect.selectedIndex] ? catSelect.options[catSelect.selectedIndex].text : '',
          matchedTaskId: taskSelect.value ? parseInt(taskSelect.value) : null,
          durationHours: parseFloat(card.querySelector('.block-duration').value) || 1.0,
          description: card.querySelector('.block-desc').value || ''
        });
      }

      var formData = new URLSearchParams();
      formData.append('action', 'confirm');
      formData.append('recordId', currentRecordId || '0');
      formData.append('blocksJson', JSON.stringify(finalBlocks));
      formData.append('_csrf', csrfToken);

      fetch('voice-timesheet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
      })
      .then(function(resp) { return resp.json(); })
      .then(function(data) {
        if (data.status === 'ok') {
          window.location.href = contextPath + '/employee/calendar';
        } else {
          alert('Save failed: ' + (data.errorMessage || 'Unknown error'));
        }
      })
      .catch(function(err) {
        alert('Network error: ' + err.message);
      });
    });

    // ── Wire Manual Entry buttons ──
    document.getElementById('addManualTopBtn').addEventListener('click', addBlankManualBlock);
    document.getElementById('addManualBottomBtn').addEventListener('click', addBlankManualBlock);

  })();
  </script>
</body>
</html>
