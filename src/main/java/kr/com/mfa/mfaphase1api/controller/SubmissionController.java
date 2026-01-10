package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.Paper;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<APIResponse<Object>> startSubmission(
            @PathVariable @NotNull UUID assessmentId
    ) {
        Object submission = submissionService.startSubmission(assessmentId);
        return buildResponse("Submission ready", submission, HttpStatus.CREATED);
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

    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @GetMapping("/{submissionId}/papers")
    @Operation(
            summary = "Get submission papers",
            description = "Retrieve all papers associated with the submission.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Papers retrieved successfully",
                            content = @Content(schema = @Schema(implementation = List.class)))
            }
    )
    public ResponseEntity<APIResponse<List<PaperResponse>>> getSubmissionPapers(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        List<PaperResponse> papers = submissionService.getSubmissionPapers(assessmentId, submissionId);
        return buildResponse("Papers retrieved successfully", papers, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @DeleteMapping("/{submissionId}")
    @Operation(
            summary = "Delete submission",
            description = "Delete a submission. Only draft submissions can be deleted.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission deleted successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteSubmission(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        submissionService.deleteSubmission(assessmentId, submissionId);
        return buildResponse("Submission deleted successfully", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{submissionId}/save")
    @Operation(
            summary = "Save submission progress",
            description = "Save the current submission progress as a draft without finalizing it.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission saved successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> saveSubmission(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        submissionService.saveSubmission(assessmentId, submissionId);
        return buildResponse("Submission saved successfully", null, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping
    @Operation(
            summary = "Get all submissions by assessment",
            description = "Returns a paginated list of submissions for a specific assessment with sorting.",
            tags = {"Submission"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<SubmissionResponse>>>> getSubmissionsByAssessmentId(
            @PathVariable UUID assessmentId,
            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "STARTED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "STARTED_AT") SubmissionProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Submissions retrieved successfully", submissionService.getAllSubmissions(assessmentId, page, size, property, direction), HttpStatus.OK);
    }

}
