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
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionTypeResponse;
import kr.com.mfa.mfaphase1api.model.enums.QuestionTypeProperty;
import kr.com.mfa.mfaphase1api.service.QuestionTypeService;
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
@RequestMapping("/api/v1/question-types")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class QuestionTypeController {

    private final QuestionTypeService questionTypeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create question type",
            description = "Creates a new question type. TYPE must be unique (case-insensitive).",
            tags = {"QuestionType"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = QuestionTypeResponse.class))),
                    @ApiResponse(responseCode = "409", description = "TYPE already exists")
            }
    )
    public ResponseEntity<APIResponse<QuestionTypeResponse>> createQuestionType(
            @RequestBody @Valid QuestionTypeRequest request
    ) {
        return buildResponse("QuestionType created",
                questionTypeService.createQuestionType(request),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List question types",
            description = "Returns a paginated list of question types with sorting.",
            tags = {"QuestionType"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<QuestionTypeResponse>>>> getAllQuestionTypes(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "TYPE")
            @RequestParam(required = false, defaultValue = "TYPE") QuestionTypeProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "QuestionTypes retrieved",
                questionTypeService.getAllQuestionTypes(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{questionTypeId}")
    @Operation(
            summary = "Get question type",
            description = "Returns a question type by its ID.",
            tags = {"QuestionType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = QuestionTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "QuestionType not found")
            }
    )
    public ResponseEntity<APIResponse<QuestionTypeResponse>> getQuestionTypeById(
            @PathVariable UUID questionTypeId
    ) {
        return buildResponse("QuestionType retrieved",
                questionTypeService.getQuestionTypeById(questionTypeId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{questionTypeId}")
    @Operation(
            summary = "Update question type",
            description = "Updates a question type by ID. TYPE must remain unique.",
            tags = {"QuestionType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = QuestionTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "QuestionType not found"),
                    @ApiResponse(responseCode = "409", description = "TYPE already exists")
            }
    )
    public ResponseEntity<APIResponse<QuestionTypeResponse>> updateQuestionTypeById(
            @PathVariable UUID questionTypeId,
            @RequestBody @Valid QuestionTypeRequest request
    ) {
        return buildResponse("QuestionType updated",
                questionTypeService.updateQuestionTypeById(questionTypeId, request),
                HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{questionTypeId}")
    @Operation(
            summary = "Delete question type",
            description = "Deletes a question type by ID.",
            tags = {"QuestionType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "QuestionType not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteQuestionTypeById(
            @PathVariable UUID questionTypeId
    ) {
        questionTypeService.deleteQuestionTypeById(questionTypeId);
        return buildResponse("QuestionType deleted", null, HttpStatus.OK);
    }
}
