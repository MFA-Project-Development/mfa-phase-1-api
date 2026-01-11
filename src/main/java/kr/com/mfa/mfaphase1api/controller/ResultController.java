package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import kr.com.mfa.mfaphase1api.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class ResultController {

    private final ResultService resultService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{submissionId}/result/grade")
    @Operation(
            summary = "Graded submission results",
            description = "Graded the results of the submission.",
            tags = {"Result"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Results graded successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> gradeSubmissionResult(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        resultService.gradeSubmissionResult(assessmentId, submissionId);
        return buildResponse("Results grades successfully", null, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'INSTRUCTOR')")
    @GetMapping("/{submissionId}/result")
    @Operation(
            summary = "Get submission result",
            description = "Retrieves the result of the submission.",
            tags = {"Result"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission result retrieved successfully")
            }
    )
    public ResponseEntity<APIResponse<SubmissionResponse>> getSubmissionResult(
            @PathVariable @NotNull UUID assessmentId,
            @PathVariable @NotNull UUID submissionId
    ) {
        return buildResponse("Submission result retrieved successfully",
                resultService.getSubmissionResult(assessmentId, submissionId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @GetMapping("/results")
    @Operation(
            summary = "Get all submissions result by assessment",
            description = "Returns a paginated list of submissions result for a specific assessment with sorting.",
            tags = {"Result"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<SubmissionResponse>>>> getAllSubmissionResults(
            @PathVariable UUID assessmentId,
            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "GRADED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "GRADED_AT") SubmissionProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Submissions retrieved successfully", resultService.getAllSubmissionResults(assessmentId, page, size, property, direction), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/result/publish")
    @Operation(
            summary = "Publish submission results",
            description = "Publishes the results of the submission. After publication, results are visible to students.",
            tags = {"Result"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Results published successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> publishSubmissionResult(
            @PathVariable @NotNull UUID assessmentId
    ) {
        resultService.publishSubmissionResult(assessmentId);
        return buildResponse("Results published successfully", null, HttpStatus.OK);
    }
}
