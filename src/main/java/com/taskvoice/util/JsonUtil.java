package com.taskvoice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {}

    public static ObjectMapper mapper() { return MAPPER; }

    public static Optional<JsonNode> parse(String json) {
        if (json == null || json.isBlank()) return Optional.empty();
        try {
            return Optional.of(MAPPER.readTree(json));
        } catch (Exception e) {
            log.warn("Failed to parse JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize to JSON", e);
            return "{}";
        }
    }

    public static ObjectNode newObject() { return MAPPER.createObjectNode(); }
    public static ArrayNode  newArray()  { return MAPPER.createArrayNode(); }

    /** Write a simple {"status":"ok","message":"..."} response */
    public static String ok(String message) {
        ObjectNode n = newObject();
        n.put("status", "ok");
        n.put("message", message);
        return toJson(n);
    }

    /** Write a simple {"status":"error","message":"..."} response */
    public static String error(String message) {
        ObjectNode n = newObject();
        n.put("status", "error");
        n.put("message", message);
        return toJson(n);
    }
}
