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
import kr.com.mfa.mfaphase1api.model.dto.request.FeedbackRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.FeedbackResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.FeedbackProperty;
import kr.com.mfa.mfaphase1api.service.FeedbackService;
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
@RequestMapping("/api/v1/answers/{answerId}/feedbacks")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create feedback",
            description = "Creates a new feedback.",
            tags = {"Feedback"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                             content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<FeedbackResponse>> createFeedback(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @RequestBody @Valid FeedbackRequest request
    ) {
        return buildResponse("Feedback created",
                feedbackService.createFeedback(answerId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping
    @Operation(
            summary = "Get all feedbacks",
            description = "Retrieves all feedbacks with optional filters for type, creator, and default status.",
            tags = {"Feedback"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<FeedbackResponse>>>> getAllFeedbacks(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") FeedbackProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Feedbacks retrieved successfully",
                feedbackService.getAllFeedbacks(answerId, page, size, property, direction),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/{feedbackId}")
    @Operation(
            summary = "Get feedback by ID",
            description = "Retrieves a single feedback  by its unique identifier.",
            tags = {"Feedback"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                             content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Feedback not found")
            }
    )
    public ResponseEntity<APIResponse<FeedbackResponse>> getFeedbackById(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,

            @Parameter(description = "Feedback ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID feedbackId
    ) {
        return buildResponse("Feedback retrieved successfully",
                feedbackService.getFeedbackById(answerId, feedbackId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{feedbackId}")
    @Operation(
            summary = "Update feedback",
            description = "Updates an existing feedback by its unique identifier.",
            tags = {"Feedback"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Feedback not found")
            }
    )
    public ResponseEntity<APIResponse<FeedbackResponse>> updateFeedback(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,
            @Parameter(description = "Feedback  ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID feedbackId,
            @RequestBody @Valid FeedbackRequest request
    ) {
        return buildResponse("Feedback updated successfully",
                feedbackService.updateFeedback(answerId, feedbackId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/{feedbackId}")
    @Operation(
            summary = "Delete feedback",
            description = "Deletes an existing feedback",
            tags = {"Feedback"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Feedback not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteFeedback(
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,
            @Parameter(description = "Feedback  ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID feedbackId
    ) {
        feedbackService.deleteFeedback(answerId, feedbackId);
        return buildResponse("Feedback deleted successfully",
                null,
                HttpStatus.OK);
    }
    
}
