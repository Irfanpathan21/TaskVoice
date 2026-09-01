package com.taskvoice.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.taskvoice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * GeminiResponseValidator — validates AI response JSON shapes before any DB write.
 * Never trust or blindly persist AI output.
 */
public final class GeminiResponseValidator {

    private static final Logger log = LoggerFactory.getLogger(GeminiResponseValidator.class);

    private GeminiResponseValidator() {}

    /**
     * Validate Voice Segmentation response.
     * Expects a JSON array of objects with: title (string). Missing duration/category is supported
     * for interactive clarification.
     */
    public static JsonNode validateVoiceSegmentation(String raw) throws GeminiValidationException {
        String cleaned = stripMarkdown(raw);
        Optional<JsonNode> parsed = JsonUtil.parse(cleaned);
        
        // If not directly an array, try extracting array substring [ ... ]
        if (parsed.isEmpty() || !parsed.get().isArray()) {
            int startIdx = cleaned.indexOf('[');
            int endIdx = cleaned.lastIndexOf(']');
            if (startIdx >= 0 && endIdx > startIdx) {
                String sub = cleaned.substring(startIdx, endIdx + 1);
                parsed = JsonUtil.parse(sub);
            }
        }

        if (parsed.isEmpty() || !parsed.get().isArray()) {
            log.error("Voice segmentation response is not a JSON array: {}", truncate(cleaned));
            throw new GeminiValidationException("AI returned an invalid format for voice segmentation.");
        }
        JsonNode arr = parsed.get();
        if (arr.isEmpty()) throw new GeminiValidationException("AI returned an empty work-entry list.");
        for (JsonNode entry : arr) {
            if (!entry.has("title") || entry.path("title").asText("").isBlank()) {
                throw new GeminiValidationException("AI returned an entry with no title.");
            }
        }
        return arr;
    }

    /**
     * Validate Rephrase response.
     * Expects a plain string (just returned as-is after trim).
     */
    public static String validateRephrase(String raw) throws GeminiValidationException {
        if (raw == null || raw.isBlank()) throw new GeminiValidationException("AI returned empty rephrase.");
        String trimmed = raw.trim();
        if (trimmed.length() > 1000) trimmed = trimmed.substring(0, 1000);
        return trimmed;
    }

    /**
     * Validate Project Summary response.
     * Expects plain text.
     */
    public static String validateProjectSummary(String raw) throws GeminiValidationException {
        if (raw == null || raw.isBlank()) throw new GeminiValidationException("AI returned empty project summary.");
        return raw.trim();
    }

    /**
     * Validate Project Sentiment response.
     * Expects: {sentiment: "POSITIVE|NEUTRAL|NEGATIVE", confidence: 0-100, explanation: "..."}
     */
    public static JsonNode validateProjectSentiment(String raw) throws GeminiValidationException {
        String cleaned = stripMarkdown(raw);
        Optional<JsonNode> parsed = JsonUtil.parse(cleaned);
        if (parsed.isEmpty() || !parsed.get().isObject()) {
            int startIdx = cleaned.indexOf('{');
            int endIdx = cleaned.lastIndexOf('}');
            if (startIdx >= 0 && endIdx > startIdx) {
                parsed = JsonUtil.parse(cleaned.substring(startIdx, endIdx + 1));
            }
        }
        if (parsed.isEmpty() || !parsed.get().isObject()) {
            throw new GeminiValidationException("AI returned invalid sentiment format.");
        }
        JsonNode obj = parsed.get();
        requireString(obj, "sentiment");
        String sentiment = obj.get("sentiment").asText();
        if (!sentiment.equals("POSITIVE") && !sentiment.equals("NEUTRAL") && !sentiment.equals("NEGATIVE")) {
            throw new GeminiValidationException("Invalid sentiment value: " + sentiment);
        }
        if (!obj.has("confidence") || !obj.get("confidence").isNumber()) {
            throw new GeminiValidationException("Missing or invalid 'confidence' in sentiment response.");
        }
        requireString(obj, "explanation");
        return obj;
    }

    /**
     * Validate Appraisal Analysis response.
     * Expects the full appraisal JSON object.
     */
    public static JsonNode validateAppraisalAnalysis(String raw) throws GeminiValidationException {
        String cleaned = stripMarkdown(raw);
        Optional<JsonNode> parsed = JsonUtil.parse(cleaned);
        if (parsed.isEmpty() || !parsed.get().isObject()) {
            int startIdx = cleaned.indexOf('{');
            int endIdx = cleaned.lastIndexOf('}');
            if (startIdx >= 0 && endIdx > startIdx) {
                parsed = JsonUtil.parse(cleaned.substring(startIdx, endIdx + 1));
            }
        }
        if (parsed.isEmpty() || !parsed.get().isObject()) {
            throw new GeminiValidationException("AI returned invalid appraisal format.");
        }
        JsonNode obj = parsed.get();
        requireString(obj, "summary");
        requireString(obj, "strengths");
        requireString(obj, "improvements");
        requireNumber(obj, "overallScore");
        double score = obj.get("overallScore").asDouble();
        if (score < 0 || score > 100) throw new GeminiValidationException("AI score out of range: " + score);
        requireString(obj, "suggestedGrade");
        requireString(obj, "promotionRecommendation");
        requireString(obj, "incrementRange");
        // Validate enum values
        String[] validGrades = {"OUTSTANDING","EXCELLENT","VERY_GOOD","GOOD","AVERAGE","NEEDS_IMPROVEMENT"};
        String grade = obj.get("suggestedGrade").asText();
        boolean validGrade = false;
        for (String g : validGrades) if (g.equals(grade)) { validGrade = true; break; }
        if (!validGrade) throw new GeminiValidationException("Invalid suggestedGrade: " + grade);
        return obj;
    }

    // ====== Helpers ======

    private static void requireString(JsonNode obj, String field) throws GeminiValidationException {
        if (!obj.has(field) || !obj.get(field).isTextual() || obj.get(field).asText().isBlank()) {
            throw new GeminiValidationException("Missing or empty required field: " + field);
        }
    }

    private static void requireNumber(JsonNode obj, String field) throws GeminiValidationException {
        if (!obj.has(field) || !obj.get(field).isNumber()) {
            throw new GeminiValidationException("Missing or non-numeric field: " + field);
        }
    }

    private static String stripMarkdown(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
    }

    private static String truncate(String s) {
        return s != null && s.length() > 200 ? s.substring(0, 200) + "..." : s;
    }
}
