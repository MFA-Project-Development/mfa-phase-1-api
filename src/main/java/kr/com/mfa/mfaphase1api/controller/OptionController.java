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
import kr.com.mfa.mfaphase1api.model.dto.request.OptionRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.UpdateOptionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.OptionResponse;
import kr.com.mfa.mfaphase1api.model.enums.OptionProperty;
import kr.com.mfa.mfaphase1api.service.OptionService;
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
@RequestMapping("/api/v1/questions/{questionId}/options")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class OptionController {

    private final OptionService optionService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    @Operation(
            summary = "Create option",
            description = "Creates a new option under a question. Text/order should be unique per question as per business rule.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = OptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Question not found"),
                    @ApiResponse(responseCode = "409", description = "Constraint violation for this question (e.g., duplicate text/order)")
            }
    )
    public ResponseEntity<APIResponse<OptionResponse>> createOption(
            @PathVariable UUID questionId,
            @RequestBody @Valid OptionRequest request
    ) {
        return buildResponse("Option created",
                optionService.createOption(questionId, request),
                HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/bulk")
    @Operation(
            summary = "Create multiple options",
            description = "Creates multiple new options under a question. Text/order should be unique per question as per business rule.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = OptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Question not found"),
                    @ApiResponse(responseCode = "409", description = "Constraint violation for this question (e.g., duplicate text/order)")
            }
    )
    public ResponseEntity<APIResponse<List<OptionResponse>>> createMultipleOptions(
            @PathVariable UUID questionId,
            @RequestBody List<@Valid OptionRequest> requests
    ) {
        return buildResponse("Options created",
                optionService.createMultipleOptions(questionId, requests),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List options",
            description = "Returns a paginated list of options for a question with sorting.",
            tags = {"Option"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<OptionResponse>>>> getAllOptions(
            @PathVariable UUID questionId,

            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "OPTION_ORDER")
            @RequestParam(required = false, defaultValue = "OPTION_ORDER") OptionProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Options retrieved",
                optionService.getAllOptions(questionId, page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{optionId}")
    @Operation(
            summary = "Get option",
            description = "Returns an option by its ID.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = OptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Option not found")
            }
    )
    public ResponseEntity<APIResponse<OptionResponse>> getOptionById(
            @PathVariable UUID questionId,
            @PathVariable UUID optionId
    ) {
        return buildResponse(
                "Option retrieved",
                optionService.getOptionById(questionId, optionId),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{optionId}")
    @Operation(
            summary = "Update option",
            description = "Updates an option by ID. Text/order must remain valid/unique within the same question.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = OptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Option/Question not found"),
                    @ApiResponse(responseCode = "409", description = "Constraint violation for this question (e.g., duplicate text/order)")
            }
    )
    public ResponseEntity<APIResponse<OptionResponse>> updateOptionById(
            @PathVariable UUID questionId,
            @PathVariable UUID optionId,
            @RequestBody @Valid OptionRequest request
    ) {
        return buildResponse(
                "Option updated",
                optionService.updateOptionById(questionId, optionId, request),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/bulk")
    @Operation(
            summary = "Update multiple options",
            description = "Updates multiple options by their IDs. Text/order must remain valid/unique within the same question.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = OptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Option/Question not found"),
                    @ApiResponse(responseCode = "409", description = "Constraint violation for this question (e.g., duplicate text/order)")
            }
    )
    public ResponseEntity<APIResponse<List<OptionResponse>>> updateMultipleOptions(
            @PathVariable UUID questionId,
            @RequestBody List<@Valid UpdateOptionRequest> requests
    ) {
        return buildResponse(
                "Options updated",
                optionService.updateMultipleOptions(questionId, requests),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{optionId}")
    @Operation(
            summary = "Delete option",
            description = "Deletes an option by ID.",
            tags = {"Option"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Option not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteOptionById(
            @PathVariable UUID questionId,
            @PathVariable UUID optionId
    ) {
        optionService.deleteOptionById(questionId, optionId);
        return buildResponse("Option deleted", null, HttpStatus.OK);
    }
}
