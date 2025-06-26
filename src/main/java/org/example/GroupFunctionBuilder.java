package org.example;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Builds grouped test functions for each Swagger tag from path definitions,
 * including status check, timing, body validation, logging response status per request,
 * and safe handling of nulls in Swagger parameter definitions.
 * Supports static/example/default/enum values for query and path parameters.
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

                // Parameter handling
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

                int status = 200;
                JsonNode responses = ep.details.get("responses");
                if (responses != null) {
                    if (responses.has("201")) status = 201;
                    else if (responses.has("204")) status = 204;
                    else if (responses.fieldNames().hasNext()) {
                        try {
                            status = Integer.parseInt(responses.fieldNames().next());
                        } catch (Exception ignored) {}
                    }
                }

                builder.append("// Endpoint: ").append(ep.path).append("\n")
                        .append("// Method: ").append(ep.method.toUpperCase()).append("\n")
                        .append(bodyBlock)
                        .append("   let response_").append(safeName).append(" = http.")
                        .append(method).append("(`${BASE_URL}").append(finalPath).append(queryParams).append("`, ")
                        .append((hasBody && !"get".equals(method)) ? "body_" + safeName + ", { headers: HEADERS }" : "{ headers: HEADERS }")
                        .append(");\n")
                        .append("   check(response_").append(safeName).append(", {\n")
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" status is ").append(status).append("': (r) => { const ok = r.status === ").append(status)
                        .append("; if (!ok) console.log(`[FAIL][status is ").append(status)
                        .append("][var=response_").append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url} - got ${r.status}`); return ok; },\n")

                        // 401
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" status is 401': (r) => { const ok = r.status === 401; if (ok) console.log(`[INFO][status is 401][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url}`); return ok; },\n")

                        // 404
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" status is 404': (r) => { const ok = r.status === 404; if (ok) console.log(`[INFO][status is 404][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url}`); return ok; },\n")

                        // 500
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" status is 500': (r) => { const ok = r.status === 500; if (ok) console.log(`[INFO][status is 500][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url}`); return ok; },\n")

                        // Response has body
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" response has body': (r) => { const ok = r.body && r.body.length > 0; if (!ok) console.log(`[FAIL][no body][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url}`); return ok; },\n")

                        // Content-type is JSON
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" content-type is JSON': (r) => { const ok = r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'); ")
                        .append("if (!ok) console.log(`[FAIL][unexpected content-type][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] Expected JSON, got ${r.headers['Content-Type']} for ${r.request.url}`); return ok; },\n")

                        // Response timing
                        .append("  '[")
                        .append(ep.method.toUpperCase()).append("] ")
                        .append(finalPath).append(queryParams)
                        .append(" response < 500ms': (r) => { const ok = r.timings.duration < 500; if (!ok) console.log(`[SLOW][${r.timings.duration}ms][var=response_")
                        .append(safeName).append("][method=").append(ep.method.toUpperCase())
                        .append("] ${r.request.method} ${r.request.url}`); return ok; },\n")

                        .append("});\n")
                        .append("   sleep(1);\n\n");
            }

            output.put(groupName, builder);
        }

        return output;
    }
}