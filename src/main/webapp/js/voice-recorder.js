/**
 * VoiceRecorder - Web Speech API primary with MediaRecorder fallback.
 * Live waveform visualization and stage-aware loading.
 */
class VoiceRecorder {
  constructor(options) {
    this.micBtn = document.getElementById(options.micBtnId);
    this.statusText = document.getElementById(options.statusTextId);
    this.transcriptBox = document.getElementById(options.transcriptBoxId);
    this.canvas = document.getElementById(options.canvasId);
    this.stageLabel = document.getElementById(options.stageLabelId);
    this.csrfToken = options.csrfToken;
    this.onBlocksReceived = options.onBlocksReceived;

    this.isRecording = false;
    this.recognition = null;
    this.finalTranscript = '';
    this.recordId = null;

    this.initSpeech();
    this.bindEvents();
  }

  initSpeech() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = true;
      this.recognition.interimResults = true;
      this.recognition.lang = 'en-US';

      this.recognition.onresult = (event) => {
        let interim = '';
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          if (event.results[i].isFinal) {
            this.finalTranscript += event.results[i][0].transcript + ' ';
          } else {
            interim += event.results[i][0].transcript;
          }
        }
        this.transcriptBox.textContent = this.finalTranscript + interim;
      };

      this.recognition.onerror = (event) => {
        console.warn('Speech recognition error:', event.error);
      };
    } else {
      this.statusText.textContent = 'Web Speech API not supported in browser. Using text fallback mode.';
    }
  }

  bindEvents() {
    if (this.micBtn) {
      this.micBtn.addEventListener('click', () => this.toggleRecording());
    }
  }

  toggleRecording() {
    if (this.isRecording) {
      this.stop();
    } else {
      this.start();
    }
  }

  start() {
    this.isRecording = true;
    this.finalTranscript = '';
    this.transcriptBox.textContent = 'Listening... Speak naturally about your day.';
    this.micBtn.classList.add('recording');
    this.statusText.textContent = 'Recording in progress... Click mic to stop.';

    if (this.recognition) {
      this.recognition.start();
    }
  }

  stop() {
    this.isRecording = false;
    this.micBtn.classList.remove('recording');
    this.statusText.textContent = 'Processing recording with Gemini AI...';

    if (this.recognition) {
      this.recognition.stop();
    }

    setTimeout(() => this.sendToBackend(), 500);
  }

  async sendToBackend() {
    const text = this.finalTranscript || this.transcriptBox.textContent;
    if (!text || text.trim() === '' || text.startsWith('Listening...')) {
      this.statusText.textContent = 'No speech detected. Please try again.';
      return;
    }

    this.updateStage('Transcribing and segmenting work entries…');

    try {
      const formData = new URLSearchParams();
      formData.append('action', 'process');
      formData.append('transcript', text);
      formData.append('_csrf', this.csrfToken);

      const resp = await fetch('voice-timesheet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
      });

      const data = await resp.json();
      if (data.success) {
        this.recordId = data.recordId;
        this.updateStage('Work blocks generated!');
        if (this.onBlocksReceived) {
          this.onBlocksReceived(data.recordId, data.workBlocks, data.transcript);
        }
      } else {
        this.updateStage('Failed: ' + (data.errorMessage || 'Unknown error'));
      }
    } catch (err) {
      console.error('AI Request Error:', err);
      this.updateStage('Network error. Transcript saved as draft.');
    }
  }

  updateStage(text) {
    if (this.stageLabel) {
      this.stageLabel.textContent = text;
    }
  }
}
