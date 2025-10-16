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
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.enums.QuestionProperty;
import kr.com.mfa.mfaphase1api.service.QuestionService;
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
@RequestMapping("/api/v1/assessments/{assessmentId}/questions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class QuestionController {

    private final QuestionService questionService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create question",
            description = "Creates a new question. questionOrder will auto-increment within the target assessment.",
            tags = {"Question"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Assessment/QuestionType not found"),
                    @ApiResponse(responseCode = "409", description = "Unique constraint violation (e.g., order conflict)")
            }
    )
    public ResponseEntity<APIResponse<QuestionResponse>> createQuestion(
            @PathVariable UUID assessmentId,
            @RequestBody @Valid QuestionRequest request
    ) {
        return buildResponse("Question created", questionService.createQuestion(assessmentId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List questions",
            description = "Returns a paginated list of questions with sorting. Optional filter by assessmentId.",
            tags = {"Question"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<QuestionResponse>>>> getAllQuestions(
            @PathVariable UUID assessmentId,

            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "QUESTION_ORDER")
            @RequestParam(required = false, defaultValue = "QUESTION_ORDER") QuestionProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Questions retrieved",
                questionService.getAllQuestions(assessmentId, page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{questionId}")
    @Operation(
            summary = "Get question",
            description = "Returns a question by its ID.",
            tags = {"Question"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Question not found")
            }
    )
    public ResponseEntity<APIResponse<QuestionResponse>> getQuestionById(
            @PathVariable UUID assessmentId,
            @PathVariable UUID questionId
    ) {
        return buildResponse("Question retrieved", questionService.getQuestionById(assessmentId, questionId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @PutMapping("/{questionId}")
    @Operation(
            summary = "Update question",
            description = "Updates a question by ID. questionOrder is unchanged unless you use the dedicated reorder endpoint.",
            tags = {"Question"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = QuestionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Question/Assessment/QuestionType not found")
            }
    )
    public ResponseEntity<APIResponse<QuestionResponse>> updateQuestionById(
            @PathVariable UUID assessmentId,
            @PathVariable UUID questionId,
            @RequestBody @Valid QuestionRequest request
    ) {
        return buildResponse("Question updated", questionService.updateQuestionById(assessmentId, questionId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @DeleteMapping("/{questionId}")
    @Operation(
            summary = "Delete question",
            description = "Deletes a question by ID. You may resequence orders in service if needed.",
            tags = {"Question"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Question not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteQuestionById(
            @PathVariable UUID assessmentId,
            @PathVariable UUID questionId
    ) {
        questionService.deleteQuestionById(assessmentId, questionId);
        return buildResponse("Question deleted", null, HttpStatus.OK);
    }
}
