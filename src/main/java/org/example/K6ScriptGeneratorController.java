package org.example;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/k6")
@RequiredArgsConstructor
public class K6ScriptGeneratorController {

    private final K6ScriptGeneratorService generatorService;

    @GetMapping("/generate")
    public ResponseEntity<String> generateK6Script() {
        String bearerToken = "DGPu5_xebwt6hn-pCSrUSBZOHLt5cCOS6synrMioOYsnPdL46YQk-MvpopNsM7I4";
        String swaggerUrl = "https://new-api.maps.itsrv.xyz/v1-api-swagger";
        try {
            generatorService.generateK6ScriptFromSwagger(swaggerUrl, bearerToken);
            return ResponseEntity.ok("K6 script generated successfully in ~/Documents/scripts folder and opened");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}

