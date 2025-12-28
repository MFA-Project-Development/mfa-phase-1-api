package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.entity.FileMetadata;
import kr.com.mfa.mfaphase1api.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload file",
            description = "Uploads a file (image, document, etc.) and returns metadata information.",
            tags = {"File"},
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "File uploaded successfully",
                            content = @Content(schema = @Schema(implementation = FileMetadata.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file or unsupported format")
            }
    )
    public ResponseEntity<APIResponse<FileMetadata>> uploadFile(
            @RequestParam("file") @NotNull MultipartFile file
    ) {
        FileMetadata saved = fileService.uploadFile(file);
        return buildResponse("File uploaded successfully", saved, HttpStatus.CREATED);
    }

    @PostMapping(value = "/multiple-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Multiple Upload file",
            description = "Multiple Uploads a file (image, document, etc.) and returns metadata information.",
            tags = {"File"},
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "File uploaded successfully",
                            content = @Content(schema = @Schema(implementation = FileMetadata.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid file or unsupported format")
            }
    )
    public ResponseEntity<APIResponse<List<FileMetadata>>> uploadFile(
            @RequestParam("files") @NotNull @NotEmpty List<MultipartFile> files
    ) {
        List<FileMetadata> saved = fileService.multipleUploadFile(files);
        return buildResponse("File uploaded successfully", saved, HttpStatus.CREATED);
    }

    @GetMapping(value = "/preview/{file-name}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @Operation(
            summary = "Preview",
            description = "Fetches the file content by its name. Supports image preview",
            tags = {"File"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "File retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "File not found")
            }
    )
    public ResponseEntity<byte[]> getFileByFileName(
            @PathVariable("file-name") @NotNull String fileName
    ) throws IOException {
        InputStream inputStream = fileService.getFileByFileName(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .body(inputStream.readAllBytes());
    }

    @GetMapping(value = "/download/{file-name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
            summary = "Download file",
            description = "Downloads a file by its name with Content-Disposition header set to attachment.",
            tags = {"File"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
                    @ApiResponse(responseCode = "404", description = "File not found")
            }
    )
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable("file-name") @NotNull String fileName
    ) throws IOException {
        InputStream inputStream = fileService.getFileByFileName(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(inputStream.readAllBytes());
    }

    @DeleteMapping("/delete/{file-name}")
    @Operation(
            summary = "Delete file",
            description = "Deletes a file by its name.",
            tags = {"File"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "File deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "File not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteFileByFileName(@PathVariable("file-name") @NotNull String fileName) {
        fileService.deleteFileByFileName(fileName);
        return buildResponse("File delete successfully", null, HttpStatus.CREATED);
    }
}
