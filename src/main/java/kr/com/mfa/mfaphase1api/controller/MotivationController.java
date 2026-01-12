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
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationCommentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ReplyRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.MotivationCommentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import kr.com.mfa.mfaphase1api.service.MotivationService;
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
@RequestMapping("/api/v1/motivations")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class MotivationController {

    private final MotivationService motivationService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create motivation",
            description = "Creates a new motivation. Title must be unique within its scope per your domain rules.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = MotivationContentResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<MotivationContentResponse>> createMotivation(
            @RequestBody @Valid MotivationContentRequest request
    ) {
        return buildResponse("Motivation created",
                motivationService.createMotivation(request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping
    @Operation(
            summary = "Get all motivations",
            description = "Retrieves all motivations with optional filters for type, creator, and default status.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = MotivationContentResponseWithExtraInfo.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<MotivationContentResponseWithExtraInfo>>>> getAllMotivations(
            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") MotivationContentProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,

            @Parameter(description = "Motivation Content Type", in = ParameterIn.QUERY)
            @RequestParam(required = false) MotivationContentType type,

            @Parameter(description = "Created By", in = ParameterIn.QUERY)
            @RequestParam(required = false) UUID createdBy,

            @Parameter(description = "Is Default", in = ParameterIn.QUERY)
            @RequestParam(required = false) Boolean isDefault,

            @Parameter(description = "Bookmarked", in = ParameterIn.QUERY)
            @RequestParam(required = false) Boolean isBookmarked
    ) {
        return buildResponse("Motivations retrieved successfully",
                motivationService.getAllMotivations(page, size, property, direction, type, createdBy, isDefault, isBookmarked),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/{motivationContentId}")
    @Operation(
            summary = "Get motivation by ID",
            description = "Retrieves a single motivation content by its unique identifier.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = MotivationContentResponseWithExtraInfo.class))),
                    @ApiResponse(responseCode = "404", description = "Motivation not found")
            }
    )
    public ResponseEntity<APIResponse<MotivationContentResponseWithExtraInfo>> getMotivationById(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        return buildResponse("Motivation retrieved successfully",
                motivationService.getMotivationById(motivationContentId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{motivationContentId}")
    @Operation(
            summary = "Update motivation",
            description = "Updates an existing motivation content by its unique identifier.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = MotivationContentResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Motivation not found")
            }
    )
    public ResponseEntity<APIResponse<MotivationContentResponse>> updateMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,
            @RequestBody @Valid MotivationContentRequest request
    ) {
        return buildResponse("Motivation updated successfully",
                motivationService.updateMotivation(motivationContentId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/{motivationContentId}")
    @Operation(
            summary = "Delete motivation",
            description = "Deletes an existing motivation content by its unique identifier.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Motivation not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        motivationService.deleteMotivation(motivationContentId);
        return buildResponse("Motivation deleted successfully",
                null,
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @PostMapping("/{motivationContentId}/boookmarks")
    @Operation(
            summary = "Bookmark motivation",
            description = "Bookmarks a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bookmarked"),
            }
    )
    public ResponseEntity<APIResponse<Void>> bookmarkMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        motivationService.bookmarkMotivation(motivationContentId);
        return buildResponse("Motivation bookmarked",
                null,
                HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @DeleteMapping("/{motivationContentId}/boookmarks")
    @Operation(
            summary = "Remove bookmark motivation",
            description = "Removes a bookmark from a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bookmark removed"),
            }
    )
    public ResponseEntity<APIResponse<Void>> removeBookmarkMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        motivationService.removeBookmarkMotivation(motivationContentId);
        return buildResponse("Motivation bookmark removed",
                null,
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @PostMapping("/{motivationContentId}/comments")
    @Operation(
            summary = "Comment on motivation",
            description = "Adds a comment to a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Comment created"),
            }
    )
    public ResponseEntity<APIResponse<MotivationCommentResponse>> commentMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,

            @RequestBody @Valid MotivationCommentRequest request
    ) {
        return buildResponse("Motivation comment created",
                motivationService.commentMotivation(motivationContentId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/{motivationContentId}/comments")
    @Operation(
            summary = "Get all comments by motivation content ID",
            description = "Retrieves all comments for a specific motivation content with pagination and sorting.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<MotivationCommentResponseWithReply>>>> getAllCommentsByMotivationContentId(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,

            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property (e.g., createdAt, updatedAt)", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") MotivationCommentProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Comments retrieved successfully",
                motivationService.getAllCommentsByMotivationContentId(motivationContentId, page, size, property, direction),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @PutMapping("/{motivationContentId}/comments/{commentId}")
    @Operation(
            summary = "Update comment on motivation",
            description = "Updates a comment on a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comment updated"),
                    @ApiResponse(responseCode = "404", description = "Comment not found")
            }
    )
    public ResponseEntity<APIResponse<MotivationCommentResponse>> updateMotivationComment(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,

            @Parameter(description = "Comment ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID commentId,

            @RequestBody @Valid MotivationCommentRequest request
    ) {
        return buildResponse("Motivation comment updated",
                motivationService.updateMotivationComment(motivationContentId, commentId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @DeleteMapping("/{motivationContentId}/comments/{commentId}")
    @Operation(
            summary = "Delete comment on motivation",
            description = "Deletes a comment from a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Comment deleted"),
                    @ApiResponse(responseCode = "404", description = "Comment not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteMotivationComment(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,

            @Parameter(description = "Comment ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID commentId
    ) {
        motivationService.deleteMotivationComment(motivationContentId, commentId);
        return buildResponse("Motivation comment deleted",
                null,
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @PostMapping("/{motivationContentId}/comments/reply")
    @Operation(
            summary = "Reply to comment on motivation",
            description = "Adds a reply to a comment on a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reply created"),
            }
    )
    public ResponseEntity<APIResponse<MotivationCommentResponse>> replyCommentMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId,

            @RequestBody @Valid ReplyRequest request
    ) {
        return buildResponse("Motivation reply created",
                motivationService.replyCommentMotivation(motivationContentId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @PostMapping("/{motivationContentId}/likes")
    @Operation(
            summary = "Like motivation",
            description = "Likes a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Liked"),
            }
    )
    public ResponseEntity<APIResponse<Void>> likeMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        motivationService.likeMotivation(motivationContentId);
        return buildResponse("Motivation liked",
                null,
                HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @DeleteMapping("/{motivationContentId}/likes")
    @Operation(
            summary = "Remove like motivation",
            description = "Removes a like from a motivation content for the current user.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Like removed"),
            }
    )
    public ResponseEntity<APIResponse<Void>> unlikeMotivation(
            @Parameter(description = "Motivation content ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID motivationContentId
    ) {
        motivationService.unlikeMotivation(motivationContentId);
        return buildResponse("Motivation like removed",
                null,
                HttpStatus.OK);
    }

}
