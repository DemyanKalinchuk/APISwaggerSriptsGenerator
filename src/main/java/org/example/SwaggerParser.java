package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility for fetching and parsing Swagger (OpenAPI) JSON specs.
 * Refactored for Java 21.
 */
public class SwaggerParser {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerParser.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public SwaggerParser() {
        // Default constructor
    }

    /**
     * Fetches and parses Swagger JSON from the provided URL using Java 21 HttpClient.
     * @param swaggerUrl URL to the Swagger (OpenAPI) definition
     * @return Parsed JsonNode of the Swagger spec
     * @throws IOException if the request fails or the content is invalid
     */
    public JsonNode fetchSwaggerJson(final String swaggerUrl) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(swaggerUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Non-200 response from Swagger URL: {} (status: {})", swaggerUrl, response.statusCode());
                throw new IOException("Received status code: " + response.statusCode());
            }

            logger.info("Successfully retrieved Swagger JSON from: {}", swaggerUrl);
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            logger.error("Failed to retrieve Swagger JSON: {}", e.getMessage());
            throw new IOException("Unable to connect to Swagger URL", e);
        }
    }
}
