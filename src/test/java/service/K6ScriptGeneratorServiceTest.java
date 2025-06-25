package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.K6ScriptGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.qameta.allure.*;

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
                () -> service.generateK6ScriptFromSwagger("https://new-api.maps.itsrv.xyz/v1-api-swagger", "DGPu5_xebwt6hn-pCSrUSBZOHLt5cCOS6synrMioOYsnPdL46YQk-MvpopNsM7I4"));
        assertTrue(exception.getMessage().contains("token must not be empty"));
    }

//    @Test
//    @Story("Basic Script Generation")
//    @Severity(SeverityLevel.NORMAL)
//    @Description("Generate K6 script from simple Swagger with one endpoint")
//    void testSwaggerJsonRetrievalAndScriptGeneration(@TempDir Path tempDir) throws Exception {
//        String swaggerJson = """
//            {
//              "paths": {
//                "/pets": {
//                  "get": {}
//                }
//              }
//            }
//        """;
//        try (MockedStatic<Request> requestMock = Mockito.mockStatic(Request.class)) {
//            Request mockRequest = Mockito.mock(Request.class);
//            Content mockContent = Mockito.mock(Content.class);
//
//            Mockito.when(mockContent.asString()).thenReturn(swaggerJson);
//
//            Response mockResponse = Mockito.mock(Response.class);
//            Mockito.when(mockResponse.returnContent()).thenReturn(mockContent);
//            Mockito.when(mockRequest.execute()).thenReturn(mockResponse);
//
//            requestMock.when(() -> Request.Get(Mockito.anyString())).thenReturn(mockRequest);
//
//            service.generateK6ScriptFromSwagger("https://new-api.maps.itsrv.xyz/v1-api-swagger", "DGPu5_xebwt6hn-pCSrUSBZOHLt5cCOS6synrMioOYsnPdL46YQk-MvpopNsM7I4");
//
//            File outputFile = new File(System.getProperty("user.home") + "/Documents/scripts/generatedK6Script.js");
//            assertTrue(outputFile.exists());
//            assertTrue(outputFile.length() > 0);
//        }
//    }
//
//    @Test
//    @Story("Tag Grouping")
//    @Severity(SeverityLevel.NORMAL)
//    @Description("Test Swagger with multiple tags and verify grouping logic")
//    void testSwaggerTagGroupingWithMultipleEndpoints() throws Exception {
//        String swaggerJson = """
//              {
//              "paths": {
//                "/pets": {
//                  "get": { "tags": ["animals"] }
//                },
//                "/users": {
//                  "post": { "tags": ["people"] }
//                }
//              }
//            }
//        """;
//
//        try (MockedStatic<Request> requestMock = Mockito.mockStatic(Request.class)) {
//            Request mockRequest = Mockito.mock(Request.class);
//            Content mockContent = Mockito.mock(Content.class);
//
//            Mockito.when(mockContent.asString()).thenReturn(swaggerJson);
//
//            Response mockResponse = Mockito.mock(Response.class);
//            Mockito.when(mockResponse.returnContent()).thenReturn(mockContent);
//            Mockito.when(mockRequest.execute()).thenReturn(mockResponse);
//
//            requestMock.when(() -> Request.Get(Mockito.anyString())).thenReturn(mockRequest);
//
//            service.generateK6ScriptFromSwagger("https://new-api.maps.itsrv.xyz/v1-api-swagger", "DGPu5_xebwt6hn-pCSrUSBZOHLt5cCOS6synrMioOYsnPdL46YQk-MvpopNsM7I4");
//
//            File outputFile = new File(System.getProperty("user.home") + "/Documents/scripts/generatedK6Script.js");
//            assertTrue(outputFile.exists());
//            String content = java.nio.file.Files.readString(outputFile.toPath());
//            assertTrue(content.contains("group('animals'"));
//            assertTrue(content.contains("group('people'"));
//        }
//    }
}
