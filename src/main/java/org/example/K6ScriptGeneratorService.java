package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Central service that coordinates Swagger parsing and K6 script generation using modular classes.
 * Refactored for Java 21.
 */
@Service
public class K6ScriptGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(K6ScriptGeneratorService.class);
    private static final String OUTPUT_DIR = System.getProperty("user.home") + "/Documents/scripts";

    private final ObjectMapper objectMapper;

    public K6ScriptGeneratorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Generates a K6 script from the Swagger (OpenAPI) specification.
     *
     * @param swaggerUrl  the URL of the Swagger JSON
     * @param bearerToken the Bearer token for authorization
     * @throws Exception if parsing or writing fails
     */
    public void generateK6ScriptFromSwagger(String swaggerUrl, String bearerToken) throws Exception {
        logger.info("Fetching and parsing Swagger spec...");
        var parser = new SwaggerParser();
        JsonNode rootNode = parser.fetchSwaggerJson(swaggerUrl);

        JsonNode paths = rootNode.get("paths");
        JsonNode components = rootNode.path("components").path("schemas");

        String companyValue = swaggerUrl.contains("new-api.maps.itsrv.xyz") ? "unicorn" : "my-company";
        String baseUrl = "https://new-api.maps.itsrv.xyz";

        logger.info("Generating grouped JS code blocks...");
        var groupBuilder = new GroupFunctionBuilder(paths, components);
        Map<String, StringBuilder> groupedFunctions = groupBuilder.buildGroupedFunctions();

        logger.info("Assembling final script...");
        var scriptBuilder = new ScriptBuilder();
        String finalScript = scriptBuilder.buildFullScript(groupedFunctions, baseUrl, bearerToken, companyValue);

        Path outputDir = Path.of(OUTPUT_DIR);
        Files.createDirectories(outputDir);
        File outputFile = outputDir.resolve("generatedK6Script.js").toFile();

        try (var writer = new FileWriter(outputFile)) {
            writer.write(finalScript);
            logger.info("K6 script saved to: {}", outputFile.getAbsolutePath());
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputFile);
                logger.info("K6 script opened in default editor");
            }
        } catch (Exception e) {
            logger.warn("Could not open the file automatically: {}", e.getMessage());
        }
    }
}