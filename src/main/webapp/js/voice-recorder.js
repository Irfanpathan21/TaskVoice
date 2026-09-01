/**
 * VoiceRecorder — High-reliability voice capture engine for TaskVoice.
 * Dual Speech Architecture:
 * 1. Web Speech API (live visual feedback in transcript box)
 * 2. MediaRecorder (high-fidelity audio capture sent to Groq Whisper)
 */
class VoiceRecorder {
  constructor(options) {
    this.micBtn = document.getElementById(options.micBtnId);
    this.statusText = document.getElementById(options.statusTextId);
    this.transcriptBox = document.getElementById(options.transcriptBoxId);
    this.stageLabel = document.getElementById(options.stageLabelId);
    this.csrfToken = options.csrfToken;
    this.onBlocksReceived = options.onBlocksReceived;

    this.isRecording = false;
    this.recognition = null;
    this.mediaRecorder = null;
    this.mediaStream = null;
    this.audioChunks = [];
    this.audioMimeType = 'audio/webm';
    this.finalTranscript = '';
    this.recordId = null;

    this.initSpeech();
    this.bindEvents();
  }

  initSpeech() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      try {
        this.recognition = new SpeechRecognition();
        this.recognition.continuous = true;
        this.recognition.interimResults = true;
        this.recognition.lang = 'en-IN';

        this.recognition.onresult = (event) => {
          let interim = '';
          for (let i = event.resultIndex; i < event.results.length; ++i) {
            if (event.results[i].isFinal) {
              this.finalTranscript += event.results[i][0].transcript + ' ';
            } else {
              interim += event.results[i][0].transcript;
            }
          }
          const fullText = (this.finalTranscript + ' ' + interim).trim();
          if (this.transcriptBox) {
            this.transcriptBox.textContent = fullText;
          }
        };

        this.recognition.onerror = (e) => {
          console.warn('Web Speech API error:', e.error);
        };
      } catch (err) {
        console.warn('Could not initialize SpeechRecognition:', err);
      }
    }
  }

  bindEvents() {
    if (this.micBtn) {
      this.micBtn.addEventListener('click', () => this.toggleRecording());
    }
    const processBtn = document.getElementById('processTextBtn');
    if (processBtn) {
      processBtn.addEventListener('click', () => {
        if (this.statusText) this.statusText.textContent = 'Processing typed text...';
        this.sendToBackend(null, null);
      });
    }
  }

  toggleRecording() {
    if (this.isRecording) {
      this.stop();
    } else {
      this.start();
    }
  }

  async start() {
    this.isRecording = true;
    this.finalTranscript = '';
    this.audioChunks = [];
    this.audioBase64 = null;

    if (this.transcriptBox) {
      const currentText = this.transcriptBox.textContent || '';
      if (currentText.includes('appear here') || currentText.startsWith('Listening...') || currentText.trim() === '') {
        this.transcriptBox.textContent = '';
      }
    }

    if (this.micBtn) this.micBtn.classList.add('recording');
    if (this.statusText) this.statusText.textContent = '🔴 Recording... Speak now. (Click mic again to stop)';

    // 1. Start Web Speech API for live preview
    if (this.recognition) {
      try { this.recognition.start(); } catch (e) {}
    }

    // 2. Start MediaRecorder for Groq Whisper audio capture
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      try {
        this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });

        // Probe supported MIME types for cross-browser compatibility (Chrome, Safari, Firefox)
        let mime = '';
        if (typeof MediaRecorder !== 'undefined' && typeof MediaRecorder.isTypeSupported === 'function') {
          const candidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4', 'audio/ogg;codecs=opus', 'audio/ogg'];
          for (const c of candidates) {
            if (MediaRecorder.isTypeSupported(c)) { mime = c; break; }
          }
        }

        const options = mime ? { mimeType: mime } : {};
        this.mediaRecorder = new MediaRecorder(this.mediaStream, options);
        this.audioMimeType = this.mediaRecorder.mimeType || mime || 'audio/webm';

        this.mediaRecorder.ondataavailable = (event) => {
          if (event.data && event.data.size > 0) {
            this.audioChunks.push(event.data);
          }
        };

        // Collect audio slice every 1000ms
        this.mediaRecorder.start(1000);
      } catch (err) {
        console.error('Microphone access error:', err);
        this.isRecording = false;
        if (this.micBtn) this.micBtn.classList.remove('recording');
        if (this.statusText) {
          this.statusText.textContent = '⚠️ Microphone access denied or not available. Please check browser permissions.';
        }
      }
    } else {
      if (this.statusText) {
        this.statusText.textContent = '⚠️ Audio recording not supported in this browser. Please use Chrome, Edge, or Safari over HTTPS/localhost.';
      }
    }
  }

  stop() {
    this.isRecording = false;
    if (this.micBtn) this.micBtn.classList.remove('recording');
    if (this.statusText) this.statusText.textContent = 'Processing voice audio with Groq Whisper & Gemini...';

    if (this.recognition) {
      try { this.recognition.stop(); } catch (e) {}
    }

    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      // Define onstop callback BEFORE calling stop()
      this.mediaRecorder.onstop = () => {
        const blob = new Blob(this.audioChunks, { type: this.audioMimeType });

        // Release microphone stream tracks AFTER blob construction is complete
        if (this.mediaStream) {
          this.mediaStream.getTracks().forEach(track => track.stop());
          this.mediaStream = null;
        }

        const reader = new FileReader();
        reader.onloadend = () => {
          this.audioBase64 = reader.result;
          this.sendToBackend(this.audioBase64, this.audioMimeType);
        };
        reader.onerror = () => {
          console.error('FileReader error reading audio blob');
          this.sendToBackend(null, null);
        };
        reader.readAsDataURL(blob);
      };

      this.mediaRecorder.stop();
    } else {
      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach(track => track.stop());
        this.mediaStream = null;
      }
      setTimeout(() => this.sendToBackend(null, null), 300);
    }
  }

  async sendToBackend(audioBase64, mimeType) {
    const boxText = this.transcriptBox ? (this.transcriptBox.textContent || '').trim() : '';
    const text = boxText || this.finalTranscript.trim();

    if (!text && (!audioBase64 || audioBase64 === '')) {
      if (this.statusText) this.statusText.textContent = 'No voice audio or text detected. Please speak or type manually.';
      return;
    }

    this.updateStage('Transcribing audio via Groq Whisper & generating work entries…');

    try {
      const formData = new URLSearchParams();
      formData.append('action', 'process');
      formData.append('transcript', text);
      if (audioBase64) formData.append('audioBase64', audioBase64);
      if (mimeType) formData.append('mimeType', mimeType);
      formData.append('_csrf', this.csrfToken);

      const resp = await fetch('voice-timesheet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
      });

      if (!resp.ok) {
        this.updateStage('Server error (' + resp.status + '). Please try again.');
        return;
      }

      const data = await resp.json();
      if (data.success) {
        this.recordId = data.recordId;
        this.updateStage('✅ Groq Whisper & Gemini processing complete!');
        if (this.onBlocksReceived) {
          this.onBlocksReceived(data.recordId, data.workBlocks, data.transcript);
        }
      } else {
        this.updateStage('⚠️ ' + (data.errorMessage || 'Unknown processing error'));
      }
    } catch (err) {
      console.error('Voice Processing Error:', err);
      this.updateStage('Network error: ' + err.message + '. Transcript preserved.');
    }
  }

  updateStage(text) {
    if (this.stageLabel) {
      this.stageLabel.textContent = text;
    }
  }
}
