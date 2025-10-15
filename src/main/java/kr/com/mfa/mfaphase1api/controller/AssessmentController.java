package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.service.AssessmentService;
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
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create assessment",
            description = "Creates a new assessment. Title must be unique within its scope per your domain rules.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Related entity not found (e.g., class/sub-subject/type)"),
                    @ApiResponse(responseCode = "409", description = "Duplicate constraint violated")
            }
    )
    public ResponseEntity<APIResponse<AssessmentResponse>> createAssessment(
            @RequestBody @Valid AssessmentRequest request
    ) {
        return buildResponse("Assessment created",
                assessmentService.createAssessment(request),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List assessments",
            description = "Returns a paginated list of assessments with sorting.",
            tags = {"Assessment"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AssessmentResponse>>>> getAllAssessments(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "TITLE")
            @RequestParam(required = false, defaultValue = "TITLE") AssessmentProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "DESC")
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse(
                "Assessments retrieved",
                assessmentService.getAllAssessments(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{assessment-id}")
    @Operation(
            summary = "Get assessment",
            description = "Returns an assessment by its ID.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Assessment not found")
            }
    )
    public ResponseEntity<APIResponse<AssessmentResponse>> getAssessmentById(
            @PathVariable("assessment-id") UUID assessmentId
    ) {
        return buildResponse(
                "Assessment retrieved",
                assessmentService.getAssessmentById(assessmentId),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{assessment-id}")
    @Operation(
            summary = "Update assessment",
            description = "Updates an assessment by ID.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Assessment or related entity not found"),
                    @ApiResponse(responseCode = "409", description = "Duplicate constraint violated")
            }
    )
    public ResponseEntity<APIResponse<AssessmentResponse>> updateAssessmentById(
            @PathVariable("assessment-id") UUID assessmentId,
            @RequestBody @Valid AssessmentRequest request
    ) {
        return buildResponse(
                "Assessment updated",
                assessmentService.updateAssessmentById(assessmentId, request),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{assessment-id}")
    @Operation(
            summary = "Delete assessment",
            description = "Deletes an assessment by ID.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Assessment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteAssessmentById(
            @PathVariable("assessment-id") UUID assessmentId
    ) {
        assessmentService.deleteAssessmentById(assessmentId);
        return buildResponse("Assessment deleted", null, HttpStatus.OK);
    }

}
