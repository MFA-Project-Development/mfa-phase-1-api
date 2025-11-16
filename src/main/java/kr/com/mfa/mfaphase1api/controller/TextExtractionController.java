package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.com.mfa.mfaphase1api.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/text-extractions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class TextExtractionController {

    private final TextExtractionService textExtractionService;

    @PostMapping("/images")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(
            summary = "Extract text from images",
            description = "Extracts text content from images using OCR based on provided file names",
            tags = {"Text Extraction"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Text extracted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid file name"),
                    @ApiResponse(responseCode = "404", description = "File not found")
            }
    )
    public String extractFromImage(@RequestBody List<String> fileName) {
        return textExtractionService.extractFromImage(fileName);
    }

}
