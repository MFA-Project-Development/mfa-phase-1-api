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
import kr.com.mfa.mfaphase1api.model.dto.request.AnnotationRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AnnotationResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AnnotationProperty;
import kr.com.mfa.mfaphase1api.service.AnnotationService;
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
@RequestMapping("/api/v1/answers/{answerId}/annotations")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class AnnotationController {

    private final AnnotationService annotationService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create annotation",
            description = "Creates a new annotation.",
            tags = {"Annotation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = AnnotationResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<AnnotationResponse>> createAnnotation(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @RequestBody @Valid AnnotationRequest request
    ) {
        return buildResponse("Annotation created",
                annotationService.createAnnotation(answerId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping
    @Operation(
            summary = "Get all annotations",
            description = "Retrieves all annotations with optional filters for type, creator, and default status.",
            tags = {"Annotation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnnotationResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AnnotationResponse>>>> getAllAnnotations(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") AnnotationProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Annotations retrieved successfully",
                annotationService.getAllAnnotations(answerId, page, size, property, direction),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/{annotationId}")
    @Operation(
            summary = "Get annotation by ID",
            description = "Retrieves a single annotation by its unique identifier.",
            tags = {"Annotation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnnotationResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Annotation not found")
            }
    )
    public ResponseEntity<APIResponse<AnnotationResponse>> getAnnotationById(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @Parameter(description = "Annotation ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID annotationId
    ) {
        return buildResponse("Annotation retrieved successfully",
                annotationService.getAnnotationById(answerId, annotationId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{annotationId}")
    @Operation(
            summary = "Update annotation",
            description = "Updates an existing annotation by its unique identifier.",
            tags = {"Annotation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnnotationResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Annotation not found")
            }
    )
    public ResponseEntity<APIResponse<AnnotationResponse>> updateAnnotation(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,
            @Parameter(description = "Annotation ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID annotationId,
            @RequestBody @Valid AnnotationRequest request
    ) {
        return buildResponse("Annotation updated successfully",
                annotationService.updateAnnotation(answerId, annotationId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/{annotationId}")
    @Operation(
            summary = "Delete annotation",
            description = "Deletes an existing annotation",
            tags = {"Annotation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Annotation not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteAnnotation(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,
            @Parameter(description = "Annotation ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID annotationId
    ) {
        annotationService.deleteAnnotation(answerId, annotationId);
        return buildResponse("Annotation deleted successfully",
                null,
                HttpStatus.OK);
    }

}
