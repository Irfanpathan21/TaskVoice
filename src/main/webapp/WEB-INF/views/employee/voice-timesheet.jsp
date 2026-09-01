<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Voice Timesheet — TaskVoice Signature Feature</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/design-system.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/components.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages/voice-timesheet.css">
</head>
<body>
  <div class="app-container">
    <jsp:include page="/WEB-INF/views/shared/sidebar.jsp"/>
    <div class="main-content">
      <jsp:include page="/WEB-INF/views/shared/header.jsp"/>

      <div style="padding: var(--space-xl); max-width: 900px; margin: 0 auto;">
        
        <!-- Voice Hero Surface -->
        <div class="voice-hero">
          <h1 style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">Voice Timesheet Recorder</h1>
          <p style="color: var(--text-secondary); font-size: 14px;">Speak naturally about your day. Gemini AI will segment your recap into structured entries.</p>

          <div class="mic-button-wrapper">
            <button id="micBtn" class="mic-button" title="Click to Record">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="22"/>
              </svg>
            </button>
          </div>

          <div id="statusText" style="font-weight: 500; font-size: 14px; color: var(--text-primary);">
            Press the mic button and speak about your daily activities, or type your recap manually below.
          </div>

          <div id="stageLabel" class="stage-label"></div>

          <!-- Live Transcript Box -->
          <div id="transcriptBox" class="live-transcript-box" contenteditable="true" style="min-height: 80px; outline: none; border: 1px solid var(--border); padding: 12px; border-radius: var(--radius-md); background: rgba(0,0,0,0.1); margin-top: 12px;" placeholder="Type your recap here if you don't have a microphone..."></div>
          
          <div style="margin-top: 12px; text-align: right;">
            <button id="processTextBtn" class="btn btn-secondary">Process Typed Text</button>
          </div>
        </div>

        <!-- Spoken Recap Prompting Guide Card -->
        <div class="glass-card" style="margin-top: 24px; padding: 20px; border-left: 4px solid var(--accent-blue);">
          <h3 style="font-size: 14px; font-weight: 700; display:flex; align-items:center; gap:8px; margin-bottom: 10px;">
            <span>🗣️</span> Recommended Voice Recap Structure
          </h3>
          <p style="font-size: 13px; color: var(--text-secondary); margin-bottom: 12px;">
            For the most accurate AI segmentation and task matching, include the following key details in your spoken recap:
          </p>
          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; font-size: 12px;">
            <div style="background: rgba(255,255,255,0.03); padding: 10px; border-radius: var(--radius-sm); border: 1px solid var(--border);">
              <strong style="color: var(--accent-blue); display:block; margin-bottom:4px;">1. Task / Activity Name</strong>
              State the specific feature or task (e.g., <em>"Worked on User Authentication Servlet..."</em>).
            </div>
            <div style="background: rgba(255,255,255,0.03); padding: 10px; border-radius: var(--radius-sm); border: 1px solid var(--border);">
              <strong style="color: var(--accent-emerald); display:block; margin-bottom:4px;">2. Duration in Hours</strong>
              Specify hours spent (e.g., <em>"spent 3.5 hours coding and 1.5 hours in sprint standup..."</em>).
            </div>
            <div style="background: rgba(255,255,255,0.03); padding: 10px; border-radius: var(--radius-sm); border: 1px solid var(--border);">
              <strong style="color: var(--accent-amber); display:block; margin-bottom:4px;">3. Work Category</strong>
              Mention type of work (e.g., <em>Development, Design, Meetings, Testing, Bug Fixes</em>).
            </div>
            <div style="background: rgba(255,255,255,0.03); padding: 10px; border-radius: var(--radius-sm); border: 1px solid var(--border);">
              <strong style="color: var(--accent-rose); display:block; margin-bottom:4px;">4. Blockers (Optional)</strong>
              Note any delays or issues (e.g., <em>"faced a database connection timeout..."</em>).
            </div>
          </div>
          <div style="margin-top: 12px; font-size: 12px; color: var(--text-muted); font-style: italic;">
            💡 <strong>Example Spoken Recap:</strong> <em>"Today I spent 4.5 hours coding the dark mode CSS design tokens and 2 hours debugging the authentication servlet login failure."</em>
          </div>
        </div>

        <!-- Generated Work Blocks Review Step -->
        <div id="reviewContainer" style="display: none;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h2 style="font-size: 18px; font-weight: 700;">Review Generated Work Blocks</h2>
            <button id="confirmSaveBtn" class="btn btn-primary">Confirm & Save Entries</button>
          </div>

          <div id="workBlocksGrid" class="work-blocks-grid"></div>
        </div>

      </div>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/voice-recorder.js?v=10"></script>
  <script>
    window.onerror = function(msg, url, lineNo, columnNo, error) {
      console.error('JS Error:', msg, 'at line', lineNo);
      const el = document.getElementById('statusText');
      if (el) el.textContent = '⚠️ JS Error: ' + msg + ' (line ' + lineNo + ')';
      return false;
    };
    let currentRecordId = null;
    let generatedBlocks = [];

    const recorder = new VoiceRecorder({
      micBtnId: 'micBtn',
      statusTextId: 'statusText',
      transcriptBoxId: 'transcriptBox',
      stageLabelId: 'stageLabel',
      csrfToken: '${sessionScope.csrfToken}',
      onBlocksReceived: (recordId, blocks, fullTranscript) => {
        currentRecordId = recordId;
        generatedBlocks = blocks;
        renderWorkBlocks(blocks, fullTranscript);
      }
    });

    function renderWorkBlocks(blocks, fullTranscript) {
      const container = document.getElementById('reviewContainer');
      const grid = document.getElementById('workBlocksGrid');
      grid.innerHTML = '';
      container.style.display = 'block';

      blocks.forEach((b, index) => {
        const card = document.createElement('div');
        card.className = 'work-block-card';
        card.innerHTML = 
          '<span class="original-phrase-tag">Original Spoken Input: "' + escapeHtml(fullTranscript) + '"</span>' +
          '<div style="margin-bottom: 12px;">' +
            '<label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px;">ENTRY TITLE</label>' +
            '<input type="text" class="form-control block-title" value="' + escapeHtml(b.title) + '"/>' +
          '</div>' +
          '<div style="display:flex; gap:12px; margin-bottom:12px;">' +
            '<div style="flex:1;">' +
              '<label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px;">CATEGORY</label>' +
              '<input type="text" class="form-control block-category" value="' + escapeHtml(b.category) + '"/>' +
            '</div>' +
            '<div style="width:100px;">' +
              '<label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px;">HOURS</label>' +
              '<input type="number" step="0.5" class="form-control block-duration" value="' + b.durationHours + '"/>' +
            '</div>' +
          '</div>' +
          '<div style="margin-bottom: 12px;">' +
            '<label style="display:block; font-size:11px; color:var(--text-secondary); margin-bottom:4px;">AI-REPHRASED DESCRIPTION</label>' +
            '<textarea class="form-control block-desc" rows="2">' + escapeHtml(b.description) + '</textarea>' +
          '</div>' +
          '<button type="button" class="btn btn-secondary" onclick="this.closest(\'.work-block-card\').remove()" style="font-size:11px; color:var(--accent-rose);">Remove Block</button>';
        grid.appendChild(card);
      });
    }

    document.getElementById('confirmSaveBtn').addEventListener('click', async () => {
      const cards = document.querySelectorAll('.work-block-card');
      const finalBlocks = [];

      cards.forEach(card => {
        finalBlocks.push({
          title: card.querySelector('.block-title').value,
          category: card.querySelector('.block-category').value,
          durationHours: parseFloat(card.querySelector('.block-duration').value),
          description: card.querySelector('.block-desc').value
        });
      });

      const formData = new URLSearchParams();
      formData.append('action', 'confirm');
      formData.append('recordId', currentRecordId);
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
      return str ? str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;") : '';
    }
  </script>
</body>
</html>
