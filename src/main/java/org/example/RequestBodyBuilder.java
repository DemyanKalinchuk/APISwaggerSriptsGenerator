package org.example;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

/**
 * Utility to build a sample request body for endpoints based on Swagger request schema.
 * Refactored for Java 21 using enhanced String handling.
 */
public class RequestBodyBuilder {

    public String buildBodyJson(JsonNode requestBody, JsonNode components) {
        StringBuilder bodyBuilder = new StringBuilder("{");

        JsonNode content = requestBody.get("content");
        JsonNode appJson = content != null ? content.get("application/json") : null;

        if (appJson != null && appJson.has("schema")) {
            JsonNode schema = appJson.get("schema");

            if (schema.has("$ref")) {
                String refName = schema.get("$ref").asText().replace("#/components/schemas/", "");
                schema = components.get(refName);
            }

            JsonNode props = schema.get("properties");
            if (props != null) {
                var fields = props.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String name = field.getKey();
                    String type = field.getValue().path("type").asText("string");
                    String value = switch (type) {
                        case "integer" -> "123";
                        case "boolean" -> "true";
                        default -> "\"sample\"";
                    };

                    bodyBuilder.append("\"%s\": %s".formatted(name, value));
                    if (fields.hasNext()) bodyBuilder.append(", ");
                }
            }
        }

        bodyBuilder.append("}");
        return bodyBuilder.toString();
    }
}
