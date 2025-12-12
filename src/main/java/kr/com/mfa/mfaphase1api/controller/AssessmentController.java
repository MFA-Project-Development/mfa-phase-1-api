package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentScheduleRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ResourceRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import kr.com.mfa.mfaphase1api.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping("/classes/{classId}/assessments")
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
            @PathVariable UUID classId,
            @RequestBody @Valid AssessmentRequest request
    ) {
        return buildResponse("Assessment created",
                assessmentService.createAssessment(classId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/classes/{classId}/assessments/{assessmentId}/schedule")
    @Operation(
            summary = "Schedule assessment",
            description = "Schedules an assessment. Title must be unique within its scope per your domain rules.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Scheduled",
                            content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Related entity not found (e.g., class/sub-subject/type)"),
                    @ApiResponse(responseCode = "409", description = "Duplicate constraint violated")
            }
    )
    public ResponseEntity<APIResponse<AssessmentResponse>> scheduleAssessment(
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId,
            @RequestBody @Valid AssessmentScheduleRequest request
    ) {
        return buildResponse("Assessment scheduled",
                assessmentService.scheduleAssessment(classId, assessmentId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/classes/{classId}/assessments/{assessmentId}/publish")
    @Operation(
            summary = "Publish assessment",
            description = "Publishes an assessment, making it visible to students.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Published",
                            content = @Content(schema = @Schema(implementation = AssessmentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Assessment not found"),
                    @ApiResponse(responseCode = "400", description = "Assessment cannot be published")
            }
    )
    public ResponseEntity<APIResponse<AssessmentResponse>> publishAssessment(
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId,
            @RequestBody LocalDateTime dueDate
    ) {
        return buildResponse("Assessment published",
                assessmentService.publishAssessment(classId, assessmentId, dueDate),
                HttpStatus.OK);
    }

    @GetMapping("/classes/{classId}/assessments")
    @Operation(
            summary = "List assessments",
            description = "Returns a paginated list of assessments with sorting.",
            tags = {"Assessment"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AssessmentResponse>>>> getAllAssessmentsByClassId(
            @PathVariable UUID classId,
            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") AssessmentProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Assessments retrieved successfully", assessmentService.getAllAssessmentsByClassId(classId, page, size, property, direction), HttpStatus.OK);
    }

    @GetMapping("/classes/{classId}/assessments/{assessmentId}")
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
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId
    ) {
        return buildResponse(
                "Assessment retrieved",
                assessmentService.getAssessmentById(classId, assessmentId),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/classes/{classId}/assessments/{assessmentId}")
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
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId,
            @RequestBody @Valid AssessmentRequest request
    ) {
        return buildResponse(
                "Assessment updated",
                assessmentService.updateAssessmentById(classId, assessmentId, request),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/classes/{classId}/assessments/{assessmentId}")
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
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId
    ) {
        assessmentService.deleteAssessmentById(classId, assessmentId);
        return buildResponse("Assessment deleted", null, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/classes/{classId}/assessments/{assessmentId}/resources")
    @Operation(
            summary = "Upload assessment resource",
            description = "Upload answer resources for the submission. Multiple files can be uploaded.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resources uploaded successfully")
            }
    )
    public ResponseEntity<APIResponse<Void>> persistAssessmentResource(
            @PathVariable @NotNull UUID classId,
            @PathVariable @NotNull UUID assessmentId,
            @RequestParam(defaultValue = "FILE") ResourceKind kind,
            @RequestBody @NotNull @NotEmpty List<@Valid ResourceRequest> requests
    ) {
        assessmentService.persistAssessmentResource(classId, assessmentId, kind, requests);
        return buildResponse("Resources uploaded successfully", null, HttpStatus.OK);
    }

    @GetMapping("/classes/{classId}/assessments/{assessmentId}/resources")
    @Operation(
            summary = "Get assessment resources",
            description = "Returns all resources associated with the assessment.",
            tags = {"Assessment"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resources retrieved successfully",
                            content = @Content(schema = @Schema(implementation = ResourceResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Assessment not found")
            }
    )
    public ResponseEntity<APIResponse<List<ResourceResponse>>> getAssessmentResources(
            @PathVariable UUID classId,
            @PathVariable UUID assessmentId
    ) {
        return buildResponse(
                "Resources retrieved successfully",
                assessmentService.getAssessmentResources(classId, assessmentId),
                HttpStatus.OK
        );
    }

    @GetMapping("/assessments")
    @Operation(
            summary = "List assessments without class",
            description = "Returns a paginated list of assessments with sorting without class.",
            tags = {"Assessment"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AssessmentResponseForGrading>>>> getAllAssessments(
            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") AssessmentProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Assessments retrieved successfully", assessmentService.getAllAssessments(page, size, property, direction), HttpStatus.OK);
    }

}
