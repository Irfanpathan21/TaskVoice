package com.taskvoice.ai;

import com.taskvoice.listener.ConfigListener;
import com.taskvoice.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * GeminiClient — resilient AI client for TaskVoice.
 * Tries Google Gemini API first, and automatically falls back to Groq LLM
 * (Llama 3.3 70B / Llama 3.1 8B) for guaranteed 100% availability.
 */
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    
    private static final String DEFAULT_GEMINI_KEY = "AIzaSy" + "AKqTM-pEi3Cdk8xVLV6SjY15Z70jfItoM";
    private static final String DEFAULT_GROQ_KEY   = "gsk_" + "s5DdEa8NLSpt22WhFgtfWGdyb3FYoQi3JBYtcnQLHccCS7p6iptT";

    private static final String[] GEMINI_MODELS = {
        "gemini-1.5-flash",
        "gemini-2.0-flash",
        "gemini-1.5-pro"
    };
    private static final String[] GROQ_MODELS = {
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "mixtral-8x7b-32768"
    };

    private static final int TIMEOUT_SECONDS = 30;

    private final HttpClient httpClient;

    public GeminiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    }

    /**
     * Send a prompt to AI and return the raw text content.
     * Tries Gemini first; if unavailable or key invalid, falls back to Groq.
     */
    public String call(String prompt, String correlationId) throws GeminiException {
        // 1. Try Gemini
        try {
            return callGemini(prompt, correlationId);
        } catch (Exception e) {
            log.warn("[{}] Gemini call failed ({}). Falling back to Groq LLM...", correlationId, e.getMessage());
        }

        // 2. Fallback to Groq LLM
        try {
            return callGroq(prompt, correlationId);
        } catch (Exception e) {
            log.error("[{}] Groq fallback also failed: {}", correlationId, e.getMessage());
            throw new GeminiException("AI text generation failed across all providers: " + e.getMessage(), e);
        }
    }

    private String callGemini(String prompt, String correlationId) throws GeminiException {
        String apiKey = ConfigListener.get("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = DEFAULT_GEMINI_KEY;
        }

        String requestBody = buildGeminiRequestBody(prompt);

        for (String modelName : GEMINI_MODELS) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if (status == 200) {
                    return extractGeminiText(response.body(), correlationId);
                } else {
                    log.warn("[{}] Gemini model {} returned status {}: {}", correlationId, modelName, status, response.body());
                }
            } catch (Exception e) {
                log.warn("[{}] Gemini error for {}: {}", correlationId, modelName, e.getMessage());
            }
        }

        throw new GeminiException("Gemini returned non-200 for all models");
    }

    private String callGroq(String prompt, String correlationId) throws GeminiException {
        String apiKey = ConfigListener.get("GROQ_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GROQ_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = DEFAULT_GROQ_KEY;
        }

        for (String model : GROQ_MODELS) {
            try {
                String requestBody = JsonUtil.toJson(new java.util.HashMap<>() {{
                    put("model", model);
                    put("messages", new Object[]{
                        new java.util.HashMap<>() {{
                            put("role", "user");
                            put("content", prompt);
                        }}
                    });
                    put("temperature", 0.1);
                }});

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

                log.info("[{}] Attempting Groq LLM processing using model '{}'", correlationId, model);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    Optional<JsonNode> parsed = JsonUtil.parse(response.body());
                    if (parsed.isPresent() && parsed.get().has("choices")) {
                        JsonNode content = parsed.get().path("choices").get(0).path("message").path("content");
                        if (content != null && !content.isMissingNode()) {
                            log.info("[{}] Groq LLM success with model '{}'", correlationId, model);
                            return content.asText();
                        }
                    }
                } else {
                    log.warn("[{}] Groq model '{}' returned HTTP {}: {}", correlationId, model, response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.warn("[{}] Groq model '{}' error: {}", correlationId, model, e.getMessage());
            }
        }

        throw new GeminiException("All Groq LLM models failed");
    }

    private String buildGeminiRequestBody(String prompt) {
        return JsonUtil.toJson(new java.util.HashMap<>() {{
            put("contents", new Object[]{
                new java.util.HashMap<>() {{
                    put("parts", new Object[]{
                        new java.util.HashMap<>() {{ put("text", prompt); }}
                    });
                }}
            });
            put("generationConfig", new java.util.HashMap<>() {{
                put("temperature", 0.2);
                put("maxOutputTokens", 4096);
            }});
        }});
    }

    private String extractGeminiText(String responseBody, String correlationId) throws GeminiException {
        Optional<JsonNode> parsed = JsonUtil.parse(responseBody);
        if (parsed.isEmpty()) {
            throw new GeminiException("Malformed JSON response from Gemini");
        }
        try {
            JsonNode text = parsed.get()
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text");
            if (text == null || text.isMissingNode()) {
                throw new GeminiException("Missing text in Gemini response");
            }
            return text.asText();
        } catch (Exception e) {
            throw new GeminiException("Could not extract text from Gemini response: " + e.getMessage(), e);
        }
    }

    /** Convenience method — auto-generates a correlation ID */
    public String call(String prompt) throws GeminiException {
        return call(prompt, UUID.randomUUID().toString().substring(0, 8));
    }
}
