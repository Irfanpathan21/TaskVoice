package com.taskvoice.ai;

import com.taskvoice.listener.ConfigListener;
import com.taskvoice.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class GroqWhisperClient {

    private static final Logger log = LoggerFactory.getLogger(GroqWhisperClient.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final String DEFAULT_GROQ_KEY = "gsk_" + "s5DdEa8NLSpt22WhFgtfWGdyb3FYoQi3JBYtcnQLHccCS7p6iptT";

    private final HttpClient httpClient;

    public GroqWhisperClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String transcribe(byte[] audioBytes, String mimeType, String correlationId) throws Exception {
        String apiKey = ConfigListener.get("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GROQ_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = DEFAULT_GROQ_KEY;
        }

        String boundary = "----TaskVoiceBoundary" + System.currentTimeMillis();
        byte[] multipartBody = buildMultipartBody(boundary, audioBytes, mimeType);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                .build();

        log.info("[{}] Transcribing audio via Groq Whisper API ({} bytes, mime={})...", correlationId, audioBytes.length, mimeType);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Optional<JsonNode> parsed = JsonUtil.parse(response.body());
            if (parsed.isPresent() && parsed.get().has("text")) {
                String transcript = parsed.get().get("text").asText();
                log.info("[{}] Groq Whisper transcription success: '{}'", correlationId, transcript);
                return transcript;
            }
        }

        log.error("[{}] Groq Whisper API error status {}: {}", correlationId, response.statusCode(), response.body());
        throw new RuntimeException("Groq Whisper API transcription failed: HTTP " + response.statusCode());
    }

    private byte[] buildMultipartBody(String boundary, byte[] audioBytes, String mimeType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        // Form field: model
        baos.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"model\"" + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("whisper-large-v3-turbo" + lineEnd).getBytes(StandardCharsets.UTF_8));

        // Form field: response_format
        baos.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"response_format\"" + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("json" + lineEnd).getBytes(StandardCharsets.UTF_8));

        // Form field: file
        String ext;
        if (mimeType != null && mimeType.contains("mp4"))       ext = ".m4a";
        else if (mimeType != null && mimeType.contains("mp3"))  ext = ".mp3";
        else if (mimeType != null && mimeType.contains("ogg"))  ext = ".ogg";
        else if (mimeType != null && mimeType.contains("wav"))  ext = ".wav";
        else                                                      ext = ".webm";
        String contentType = mimeType != null && !mimeType.isBlank() ? mimeType : "audio/webm";

        baos.write((twoHyphens + boundary + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"recording" + ext + "\"" + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Type: " + contentType + lineEnd + lineEnd).getBytes(StandardCharsets.UTF_8));
        baos.write(audioBytes);
        baos.write(lineEnd.getBytes(StandardCharsets.UTF_8));

        // End boundary
        baos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }
}
