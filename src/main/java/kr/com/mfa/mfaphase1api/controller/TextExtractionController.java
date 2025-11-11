package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.com.mfa.mfaphase1api.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/text-extractions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class TextExtractionController {

    private final TextExtractionService textExtractionService;

    @PostMapping
    public String extractFromImage(@RequestParam("file") MultipartFile file) {
//        return textExtractionService.extractFromImage(file);
        return null;
    }

}
