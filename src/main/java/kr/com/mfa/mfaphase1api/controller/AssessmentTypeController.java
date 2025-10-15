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
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentTypeProperty;
import kr.com.mfa.mfaphase1api.service.AssessmentTypeService;
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
@RequestMapping("/api/v1/assessment-types")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class AssessmentTypeController {

    private final AssessmentTypeService assessmentTypeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create assessment type",
            description = "Creates a new assessment type. Assessment Type name must be unique (case-insensitive).",
            tags = {"AssessmentType"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = AssessmentTypeResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Type already exists")
            }
    )
    public ResponseEntity<APIResponse<AssessmentTypeResponse>> createAssessmentType(
            @RequestBody @Valid AssessmentTypeRequest request
    ) {
        return buildResponse("Assessment Type Created", assessmentTypeService.createAssessmentType(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List assessment types",
            description = "Returns a paginated list of assessment types with sorting.",
            tags = {"AssessmentType"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<AssessmentTypeResponse>>>> getAllAssessmentTypes(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "TYPE")
            @RequestParam(required = false, defaultValue = "TYPE") AssessmentTypeProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Assessment Types retrieved",
                assessmentTypeService.getAllAssessmentTypes(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{assessment-type-id}")
    @Operation(
            summary = "Get assessment type",
            description = "Returns a assessment type by its ID.",
            tags = {"AssessmentType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AssessmentTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "AssessmentType not found")
            }
    )
    public ResponseEntity<APIResponse<AssessmentTypeResponse>> getAssessmentTypeById(
            @PathVariable("assessment-type-id") UUID assessmentTypeId
    ) {
        return buildResponse("Assessment Type retrieved", assessmentTypeService.getAssessmentTypeById(assessmentTypeId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{assessment-type-id}")
    @Operation(
            summary = "Update assessment type",
            description = "Updates a assessment type by ID. Assessment type must remain unique.",
            tags = {"AssessmentType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = AssessmentTypeResponse.class))),
                    @ApiResponse(responseCode = "404", description = "AssessmentType not found"),
                    @ApiResponse(responseCode = "409", description = "Type already exists")
            }
    )
    public ResponseEntity<APIResponse<AssessmentTypeResponse>> updateAssessmentTypeById(
            @PathVariable("assessment-type-id") UUID assessmentTypeId,
            @RequestBody @Valid AssessmentTypeRequest request
    ) {
        return buildResponse("Assessment Type updated", assessmentTypeService.updateAssessmentTypeById(assessmentTypeId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{assessment-type-id}")
    @Operation(
            summary = "Delete assessment type",
            description = "Deletes a assessment type by ID.",
            tags = {"AssessmentType"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "AssessmentType not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteAssessmentTypeById(
            @PathVariable("assessment-type-id") UUID assessmentTypeId
    ) {
        assessmentTypeService.deleteAssessmentTypeById(assessmentTypeId);
        return buildResponse("Assessment Type deleted", null, HttpStatus.OK);
    }
    
}
