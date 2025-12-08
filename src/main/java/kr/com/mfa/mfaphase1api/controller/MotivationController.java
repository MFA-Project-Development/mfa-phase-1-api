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
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
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
    @PostMapping("/content")
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

    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    @GetMapping("/content")
    @Operation(
            summary = "Get all motivations",
            description = "Retrieves all motivations with optional filters for type, creator, and default status.",
            tags = {"Motivation"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = MotivationContentResponse.class))),
            }
    )
    public ResponseEntity<APIResponse<PagedResponse<List<MotivationContentResponse>>>> getAllMotivations(
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
            @RequestParam(required = false) Boolean isDefault
    ) {
        return buildResponse("Motivations retrieved successfully",
                motivationService.getAllMotivations(page, size, property, direction, type, createdBy, isDefault),
                HttpStatus.OK);
    }

}
