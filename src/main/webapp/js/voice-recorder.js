/**
 * VoiceRecorder — End-to-End Voice Capture Engine for TaskVoice.
 * 
 * Workflow:
 * 1. User clicks mic → getUserMedia & MediaRecorder start.
 * 2. SpeechRecognition provides real-time live preview in transcript box.
 * 3. User stops mic → MediaRecorder finalizes audio Blob.
 * 4. Audio Blob is uploaded as binary multipart FormData directly to Servlet.
 * 5. Servlet passes audio to Groq Whisper for speech-to-text.
 * 6. Servlet passes transcript to AI for structured work-block segmentation.
 * 7. Frontend receives work entries JSON and renders editable form cards.
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

  /* ───── Web Speech API (Live Visual Preview) ───── */
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
          console.warn('Web Speech API preview error:', e.error);
        };
      } catch (err) {
        console.warn('SpeechRecognition initialization error:', err);
      }
    }
  }

  /* ───── Event Bindings ───── */
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

  /* ───── Start Recording ───── */
  async start() {
    this.isRecording = true;
    this.finalTranscript = '';
    this.audioChunks = [];

    // Clear placeholder text in transcript box
    if (this.transcriptBox) {
      const currentText = this.transcriptBox.textContent || '';
      if (currentText.includes('appear here') || currentText.startsWith('Listening...') || currentText.trim() === '') {
        this.transcriptBox.textContent = '';
      }
    }

    // UI Recording State
    if (this.micBtn) this.micBtn.classList.add('recording');
    if (this.statusText) this.statusText.textContent = '🔴 Recording... Speak now. (Click mic again to stop)';

    // Start Web Speech API live preview
    if (this.recognition) {
      try { this.recognition.start(); } catch (e) {}
    }

    // Start MediaRecorder audio capture
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      try {
        this.mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });

        // Select optimal MIME type for Chrome / Safari / Firefox
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

        // Slice audio chunks every 1000ms
        this.mediaRecorder.start(1000);
      } catch (err) {
        console.error('Microphone stream access error:', err);
        this.isRecording = false;
        if (this.micBtn) this.micBtn.classList.remove('recording');
        if (this.statusText) {
          this.statusText.textContent = '⚠️ Microphone access denied. Please allow microphone permission in your browser settings.';
        }
      }
    } else {
      if (this.statusText) {
        this.statusText.textContent = '⚠️ Audio recording not supported in this browser. Please use Chrome, Edge, or Safari over HTTPS/localhost.';
      }
    }
  }

  /* ───── Stop Recording ───── */
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
        const audioBlob = new Blob(this.audioChunks, { type: this.audioMimeType });

        // Release microphone stream tracks AFTER blob creation
        if (this.mediaStream) {
          this.mediaStream.getTracks().forEach(track => track.stop());
          this.mediaStream = null;
        }

        this.sendToBackend(audioBlob, this.audioMimeType);
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

  /* ───── Send Audio/Text to Backend ───── */
  async sendToBackend(audioBlob, mimeType) {
    const boxText = this.transcriptBox ? (this.transcriptBox.textContent || '').trim() : '';
    const text = boxText || this.finalTranscript.trim();

    if (!text && (!audioBlob || audioBlob.size === 0)) {
      if (this.statusText) this.statusText.textContent = 'No voice audio or text detected. Please speak or type manually.';
      return;
    }

    this.updateStage('Transcribing audio via Groq Whisper & generating work entries…');

    try {
      const formData = new FormData();
      formData.append('action', 'process');
      formData.append('transcript', text);
      if (mimeType) formData.append('mimeType', mimeType);
      formData.append('_csrf', this.csrfToken);

      if (audioBlob && audioBlob.size > 0) {
        const ext = mimeType && mimeType.includes('mp4') ? 'm4a' : (mimeType && mimeType.includes('ogg') ? 'ogg' : 'webm');
        formData.append('audioFile', audioBlob, 'recording.' + ext);
      }

      const resp = await fetch('voice-timesheet', {
        method: 'POST',
        headers: {
          'X-CSRF-Token': this.csrfToken
        },
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
