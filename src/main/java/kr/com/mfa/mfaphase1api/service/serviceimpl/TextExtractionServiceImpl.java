package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.service.FileService;
import kr.com.mfa.mfaphase1api.service.TextExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextExtractionServiceImpl implements TextExtractionService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private static final String USER_PROMPT = """
            Extract ONLY the readable text from this image.
            - Return plain text (no commentary or extra symbols).
            - Preserve line breaks when they are meaningful.
            - For any mathematical expressions, format them using Markdown LaTeX syntax:
              - Inline math: use `$ ... $`
              - Block math: use `$$ ... $$`
              - Add a newline before and after the math block.
            """;

    private final ChatClient chatClient;
    private final FileService fileService;

    @Override
    public String extractFromImage(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return "";
        }

        StringBuilder extractedContent = new StringBuilder();

        for (String fileName : fileNames) {
            try {
                InputStream file = fileService.getFileByFileName(fileName);
                if (file == null) {
                    log.warn("File not found: {}", fileName);
                    continue;
                }

                String contentType = determineContentType(fileName);

                if (!ALLOWED_TYPES.contains(contentType)) {
                    log.warn("File type not allowed: {} ({})", fileName, contentType);
                    continue;
                }

                Message msg = UserMessage.builder()
                        .text(USER_PROMPT)
                        .media(new Media(
                                MimeTypeUtils.parseMimeType(contentType),
                                new InputStreamResource(file)
                        ))
                        .build();

                String content = chatClient
                        .prompt()
                        .messages(msg)
                        .call()
                        .content();

                if (content != null && !content.isBlank()) {
                    extractedContent.append(content.strip()).append("\n\n");
                }

            } catch (Exception e) {
                log.error("Failed to extract text from image {}: {}", fileName, e.getMessage(), e);
            }
        }

        return extractedContent.toString().strip();
    }

    private String determineContentType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "webp" -> "image/webp";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

}