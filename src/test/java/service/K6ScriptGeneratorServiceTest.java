package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.K6ScriptGeneratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.qameta.allure.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Epic("K6 Script Generator")
@Feature("Swagger to K6 Script Conversion")
class K6ScriptGeneratorServiceTest {

    private K6ScriptGeneratorService service;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new K6ScriptGeneratorService(objectMapper);
    }

    @Test
    @Story("Validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ensure exception is thrown if bearer token is empty")
    void testEmptyBearerTokenThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> service.generateK6ScriptFromSwagger("https://new-api.maps.itsrv.xyz/v1-api-swagger", ""));
        assertTrue(exception.getMessage().contains("token must not be empty"));
    }

    @Test
    void generatesK6ScriptFile() throws Exception {
        // Use a real or dummy Swagger file (can be a small valid Swagger/OpenAPI file)
        // Here we assume such file is available at this path
        String testSwaggerURL = "https://new-api.maps.itsrv.xyz/v1-api-swagger";
        String token = "I2EkgEDlawkgmOdPrLrrRRkO24RErzGwHAmYd4EliEzTUzr8vcxLYy_lmdO4gKAI";

        // Generate K6 script (ignore actual Swagger parsing for this minimal test)
        service.generateK6ScriptFromSwagger(testSwaggerURL, token);

        // Check if the file exists
        String outputDir = System.getProperty("user.home") + "/Documents/scripts";
        File generated = new File(outputDir + "/generatedK6Script.js");
        Assertions.assertTrue(generated.exists(), "K6 script should be generated at " + generated.getAbsolutePath());

        // (Optional) Cleanup
        // generated.delete();
    }
}
