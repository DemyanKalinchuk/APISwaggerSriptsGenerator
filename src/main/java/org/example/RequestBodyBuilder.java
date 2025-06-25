package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

/**
 * Utility to build a sample request body for endpoints based on Swagger request schema.
 * Uses "example", "default", or "enum" if present, otherwise uses type fallback.
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
                    JsonNode property = field.getValue();
                    String value = "\"sample\"";

                    if (property.has("example")) {
                        JsonNode ex = property.get("example");
                        value = ex.isTextual() ? "\"" + ex.asText() + "\"" : ex.toString();
                    } else if (property.has("default")) {
                        JsonNode def = property.get("default");
                        value = def.isTextual() ? "\"" + def.asText() + "\"" : def.toString();
                    } else if (property.has("enum") && property.get("enum").isArray() && property.get("enum").size() > 0) {
                        JsonNode first = property.get("enum").get(0);
                        value = first.isTextual() ? "\"" + first.asText() + "\"" : first.toString();
                    } else {
                        String type = property.path("type").asText("string");
                        value = switch (type) {
                            case "integer" -> "123";
                            case "boolean" -> "true";
                            default -> "\"sample\"";
                        };
                    }

                    bodyBuilder.append("\"%s\": %s".formatted(name, value));
                    if (fields.hasNext()) bodyBuilder.append(", ");
                }
            }
        }

        bodyBuilder.append("}");
        return bodyBuilder.toString();
    }
}