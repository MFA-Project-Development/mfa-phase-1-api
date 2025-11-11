package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    @Operation(
            summary = "Start (or reuse) submission",
            description = "Creates a draft submission for the current student or returns the existing draft.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Submission ready",
                            content = @Content(schema = @Schema(implementation = UUID.class)))
            }
    )
    public ResponseEntity<APIResponse<UUID>> startSubmission(
            @PathVariable @NotNull UUID assessmentId
    ) {
        UUID submissionId = submissionService.startSubmission(assessmentId);
        return buildResponse("Submission ready", submissionId, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{submissionId}/papers")
    @Operation(
            summary = "Upload submission papers",
            description = "Upload answer papers for the submission. Multiple files can be uploaded.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Papers uploaded successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> persistSubmissionPapers(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId,
            @RequestBody @NotNull @NotEmpty List<String> fileNames
    ) {
        submissionService.persistSubmissionPapers(assessmentId, submissionId, fileNames);
        return buildResponse("Papers uploaded successfully", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{submissionId}/submit")
    @Operation(
            summary = "Submit final answers",
            description = "Marks the current submission as completed. After submission, no more edits are allowed.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission finalized")
            }
    )
    public ResponseEntity<APIResponse<Void>> finalizeSubmission(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        submissionService.finalizeSubmission(assessmentId, submissionId);
        return buildResponse("Submission finalized", null, HttpStatus.OK);
    }
}
