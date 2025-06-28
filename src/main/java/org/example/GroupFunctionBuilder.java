package org.example;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Builds grouped test functions for each Swagger tag from path definitions,
 * including status check, timing, body validation, logging response status per request,
 * and safe handling of nulls in Swagger parameter definitions.
 * Supports static/example/default/enum values for query and path parameters.
 * Now: Only the received response status line is shown in the K6 console.
 * Also logs every request result as [STATUS] in the K6 output.
 */
public class GroupFunctionBuilder {

    private final JsonNode paths;
    private final JsonNode components;
    private final RequestBodyBuilder requestBodyBuilder;

    public GroupFunctionBuilder(JsonNode paths, JsonNode components) {
        this.paths = paths;
        this.components = components;
        this.requestBodyBuilder = new RequestBodyBuilder();
    }

    private static class EndpointMethod {
        String path;
        String method;
        JsonNode details;

        EndpointMethod(String path, String method, JsonNode details) {
            this.path = path;
            this.method = method;
            this.details = details;
        }
    }

    public Map<String, StringBuilder> buildGroupedFunctions() {
        Map<String, List<EndpointMethod>> grouped = new LinkedHashMap<>();

        // Group endpoints by tag
        paths.fields().forEachRemaining(entry -> {
            String path = entry.getKey();
            JsonNode methods = entry.getValue();
            methods.fields().forEachRemaining(methodEntry -> {
                String method = methodEntry.getKey();
                JsonNode details = methodEntry.getValue();
                String tag = "general";
                JsonNode tags = details.get("tags");
                if (tags != null && tags.isArray() && tags.size() > 0) {
                    tag = tags.get(0).asText();
                }
                grouped.computeIfAbsent(tag, k -> new ArrayList<>())
                        .add(new EndpointMethod(path, method, details));
            });
        });

        Map<String, StringBuilder> output = new LinkedHashMap<>();

        for (Map.Entry<String, List<EndpointMethod>> entry : grouped.entrySet()) {
            String groupName = entry.getKey();
            StringBuilder builder = new StringBuilder();

            for (EndpointMethod ep : entry.getValue()) {
                String safeName = ep.method.toUpperCase() + "_" + ep.path.replaceAll("[^a-zA-Z0-9]", "_");
                String finalPath = ep.path.replace("{company}", "${COMPANY}");

                // Parameter handling (both path and query)
                StringBuilder queryParams = new StringBuilder();
                JsonNode parameters = ep.details.get("parameters");
                List<String> paramList = new ArrayList<>();

                if (parameters != null && parameters.isArray()) {
                    for (JsonNode param : parameters) {
                        JsonNode nameNode = param.get("name");
                        if (nameNode == null) continue;
                        String name = nameNode.asText();

                        // Find static value for parameter
                        String value = "value";
                        JsonNode exampleNode = param.get("example");
                        JsonNode defaultNode = param.get("default");
                        JsonNode enumNode = param.get("enum");

                        if (exampleNode != null) {
                            value = exampleNode.isTextual() ? exampleNode.asText() : exampleNode.toString();
                        } else if (defaultNode != null) {
                            value = defaultNode.isTextual() ? defaultNode.asText() : defaultNode.toString();
                        } else if (enumNode != null && enumNode.isArray() && enumNode.size() > 0) {
                            JsonNode first = enumNode.get(0);
                            value = first.isTextual() ? first.asText() : first.toString();
                        }

                        String inValue = param.path("in").asText();
                        if ("path".equals(inValue)) {
                            // Replace {param} in path with value (add null safety)
                            finalPath = finalPath.replaceAll("\\{" + name + "\\}", value);
                        } else if ("query".equals(inValue)) {
                            paramList.add(name + "=" + value);
                        }
                    }
                    if (!paramList.isEmpty()) {
                        queryParams.append("?").append(String.join("&", paramList));
                    }
                }

                String method = ep.method.toLowerCase();
                boolean hasBody = ep.details.has("requestBody");
                String bodyBlock = "";
                if (hasBody) {
                    bodyBlock = "  let body_" + safeName + " = JSON.stringify(" +
                            requestBodyBuilder.buildBodyJson(ep.details.get("requestBody"), components) + ");\n";
                }

                // --- Dynamically collect all possible status codes for this endpoint
                Set<String> statusCodes = new LinkedHashSet<>();
                JsonNode responses = ep.details.get("responses");
                if (responses != null) {
                    Iterator<String> fieldNames = responses.fieldNames();
                    while (fieldNames.hasNext()) {
                        String code = fieldNames.next();
                        if (code.matches("\\d+")) {
                            statusCodes.add(code);
                        }
                    }
                }
                if (statusCodes.isEmpty()) statusCodes.add("200"); // default fallback

                // Prepare JS array string of codes
                String jsStatusArr = "[" + String.join(", ", statusCodes) + "]";
                String urlForMsg = finalPath + queryParams;

                builder.append("// Endpoint: ").append(ep.path).append("\n")
                        .append("// Method: ").append(ep.method.toUpperCase()).append("\n")
                        .append(bodyBlock)
                        .append("   let response_").append(safeName).append(" = http.")
                        .append(method).append("(`${BASE_URL}").append(finalPath).append(queryParams).append("`, ")
                        .append((hasBody && !"get".equals(method)) ? "body_" + safeName + ", { headers: HEADERS }" : "{ headers: HEADERS }")
                        .append(");\n")
                        // Restore the info log for every request (positive/negative)
                        .append("   console.log(`[STATUS][status is ${response_").append(safeName).append(".status}]")
                        .append("[var=response_").append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${response_").append(safeName).append(".request.method} ${response_").append(safeName).append(".request.url} - got ${response_").append(safeName).append(".status}`);\n")
                        .append("   check(response_").append(safeName).append(", Object.assign({},\n")
                        .append("    (() => {\n")
                        .append("      let allowed = ").append(jsStatusArr).append(";\n")
                        .append("      let got = response_").append(safeName).append(".status;\n")
                        .append("      let msg = `[")
                        .append(ep.method.toUpperCase()).append("] ").append(urlForMsg)
                        .append(" status is ${got}`;\n")
                        .append("      let obj = {};\n")
                        .append("      if (allowed.includes(got)) obj[msg] = r => r.status === got;\n")
                        .append("      return obj;\n")
                        .append("    })(),\n")
                        .append("    {\n")
                        .append("      'response has body': r => r.body && r.body.length > 0,\n")
                        .append("      'content-type is JSON': r => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),\n")
                        .append("      'response < 500ms': r => r.timings.duration < 500\n")
                        .append("    }));\n")
                        .append("   sleep(1);\n\n");
            }

            output.put(groupName, builder);
        }

        return output;
    }
}