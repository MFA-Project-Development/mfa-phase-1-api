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
import kr.com.mfa.mfaphase1api.model.dto.request.AnswerRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AnnotationProperty;
import kr.com.mfa.mfaphase1api.model.enums.AnswerProperty;
import kr.com.mfa.mfaphase1api.service.AnswerService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class AnswerController {

    private final AnswerService answerService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping("/questions/{questionId}/answers")
    @Operation(
            summary = "Grade answer",
            description = "Grades a student's answer for an assessment.",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Answer graded successfully",
                            content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Answer not found"),
                    @ApiResponse(responseCode = "409", description = "Grading conflict")
            }
    )
    public ResponseEntity<APIResponse<AnswerResponse>> gradeAnswer(
            @Parameter(description = "Question ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID questionId,

            @RequestBody @Valid AnswerRequest request
    ) {
        return buildResponse("Assessment created",
                answerService.gradeAnswer(questionId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/questions/{questionId}/answers")
    @Operation(
            summary = "Get all answers",
            description = "Retrieves all answers with optional filters for type, creator, and default status.",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AnswerResponse>>>> getAllAnswers(
            @Parameter(description = "Question ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID questionId,

            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") AnswerProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Answers retrieved successfully",
                answerService.getAllAnswers(questionId, page, size, property, direction),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/questions/{questionId}/answers/{answerId}")
    @Operation(
            summary = "Get answer by ID",
            description = "Retrieves a single answer by its unique identifier.",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Answer not found")
            }
    )
    public ResponseEntity<APIResponse<AnswerResponse>> getAnswerById(
            @Parameter(description = "Question ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID questionId,

            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId
    ) {
        return buildResponse("Answer retrieved successfully",
                answerService.getAnswerById(questionId, answerId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/questions/{questionId}/answers/{answerId}")
    @Operation(
            summary = "Update answer",
            description = "Updates an existing answer by its unique identifier.",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Answer not found")
            }
    )
    public ResponseEntity<APIResponse<AnswerResponse>> updateAnswer(
            @Parameter(description = "Question ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID questionId,
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId,
            @RequestBody @Valid AnswerRequest request
    ) {
        return buildResponse("Answer updated successfully",
                answerService.updateAnswer(questionId, answerId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/questions/{questionId}/answers/{answerId}")
    @Operation(
            summary = "Delete answer",
            description = "Deletes an existing answer",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Answer not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteAnswer(
            @Parameter(description = "Question ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID questionId,
            @Parameter(description = "Answer ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID answerId
    ) {
        answerService.deleteAnswer(questionId, answerId);
        return buildResponse("Answer deleted successfully",
                null,
                HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    @GetMapping("/submissions/{submissionId}/answers")
    @Operation(
            summary = "Get all answers by submission",
            description = "Retrieves all answers with optional filters for type, creator, and default status.",
            tags = {"Answer"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = AnswerResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AnswerResponse>>>> getAllAnswersBySubmissionId(
            @Parameter(description = "Submission ID", required = true, in = ParameterIn.PATH)
            @PathVariable UUID submissionId,

            @Parameter(description = "1-based page index", example = "1", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", example = "10", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", example = "CREATED_AT", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "CREATED_AT") AnswerProperty property,

            @Parameter(description = "Sort direction", example = "DESC", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        return buildResponse("Answers retrieved successfully",
                answerService.getAllAnswersBySubmissionId(submissionId, page, size, property, direction),
                HttpStatus.OK);
    }

}
