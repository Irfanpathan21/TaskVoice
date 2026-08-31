/**
 * VoiceRecorder - Groq Whisper Speech-to-Text + Gemini AI Segmentation.
 * Live waveform visualization and stage-aware loading.
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
    this.audioChunks = [];
    this.audioBase64 = null;
    this.audioMimeType = 'audio/webm';
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
      if (currentText.includes('appear here') || currentText.startsWith('Listening...')) {
        this.transcriptBox.textContent = '';
      }
    }

    if (this.micBtn) this.micBtn.classList.add('recording');
    if (this.statusText) this.statusText.textContent = 'Recording with Groq Whisper... Speak now. (Click mic again to stop)';

    // Start Web Speech API for live visual preview
    if (this.recognition) {
      try { this.recognition.start(); } catch (e) {}
    }

    // Start MediaRecorder for high-fidelity audio capture (Groq Whisper)
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        this.mediaRecorder = new MediaRecorder(stream);
        this.audioMimeType = this.mediaRecorder.mimeType || 'audio/webm';
        this.mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0) this.audioChunks.push(event.data);
        };
        this.mediaRecorder.start();
      } catch (err) {
        console.warn('Microphone stream access denied for MediaRecorder:', err);
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
      this.mediaRecorder.onstop = () => {
        const blob = new Blob(this.audioChunks, { type: this.audioMimeType });
        const reader = new FileReader();
        reader.onloadend = () => {
          this.audioBase64 = reader.result;
          this.sendToBackend(this.audioBase64, this.audioMimeType);
        };
        reader.readAsDataURL(blob);
      };
      this.mediaRecorder.stop();
      if (this.mediaRecorder.stream) {
        this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
      }
    } else {
      setTimeout(() => this.sendToBackend(null, null), 400);
    }
  }

  async sendToBackend(audioBase64, mimeType) {
    let text = (this.finalTranscript + ' ' + (this.transcriptBox ? this.transcriptBox.textContent : '')).trim();
    if ((!text || text === '') && (!audioBase64 || audioBase64 === '')) {
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

      const data = await resp.json();
      if (data.success) {
        this.recordId = data.recordId;
        this.updateStage('Groq Whisper & Gemini processing complete!');
        if (this.onBlocksReceived) {
          this.onBlocksReceived(data.recordId, data.workBlocks, data.transcript);
        }
      } else {
        this.updateStage('Processing Notice: ' + (data.errorMessage || 'Unknown error'));
      }
    } catch (err) {
      console.error('Voice Processing Error:', err);
      this.updateStage('Network error. Transcript preserved.');
    }
  }

  updateStage(text) {
    if (this.stageLabel) {
      this.stageLabel.textContent = text;
    }
  }
}
