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
import kr.com.mfa.mfaphase1api.model.dto.request.SubSubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubSubjectResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubSubjectProperty;
import kr.com.mfa.mfaphase1api.service.SubSubjectService;
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
@RequestMapping("/api/v1/subjects/{subjectId}/sub-subjects")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class SubSubjectController {

    private final SubSubjectService subSubjectService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create sub-subject",
            description = "Creates a new sub-subject under a subject. Name must be unique per subject (case-insensitive).",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = SubSubjectResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Subject not found"),
                    @ApiResponse(responseCode = "409", description = "Name already exists for this subject")
            }
    )
    public ResponseEntity<APIResponse<SubSubjectResponse>> createSubSubject(
            @PathVariable UUID subjectId,
            @RequestBody @Valid SubSubjectRequest request
    ) {
        return buildResponse("SubSubject created", subSubjectService.createSubSubject(subjectId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List sub-subjects",
            description = "Returns a paginated list of sub-subjects with sorting.",
            tags = {"SubSubject"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<SubSubjectResponse>>>> getAllSubSubjects(
            @PathVariable UUID subjectId,

            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "NAME")
            @RequestParam(required = false, defaultValue = "NAME") SubSubjectProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "SubSubjects retrieved",
                subSubjectService.getAllSubSubjects(subjectId, page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{subSubjectId}")
    @Operation(
            summary = "Get sub-subject",
            description = "Returns a sub-subject by its ID.",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = SubSubjectResponse.class))),
                    @ApiResponse(responseCode = "404", description = "SubSubject not found")
            }
    )
    public ResponseEntity<APIResponse<SubSubjectResponse>> getSubSubjectById(
            @PathVariable UUID subjectId,
            @PathVariable UUID subSubjectId
    ) {
        return buildResponse("SubSubject retrieved", subSubjectService.getSubSubjectById(subjectId, subSubjectId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{subSubjectId}")
    @Operation(
            summary = "Update sub-subject",
            description = "Updates a sub-subject by ID. Name must remain unique within the same subject.",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = SubSubjectResponse.class))),
                    @ApiResponse(responseCode = "404", description = "SubSubject/Subject not found"),
                    @ApiResponse(responseCode = "409", description = "Name already exists for this subject")
            }
    )
    public ResponseEntity<APIResponse<SubSubjectResponse>> updateSubSubjectById(
            @PathVariable UUID subjectId,
            @PathVariable UUID subSubjectId,
            @RequestBody @Valid SubSubjectRequest request
    ) {
        return buildResponse("SubSubject updated", subSubjectService.updateSubSubjectById(subjectId, subSubjectId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{subSubjectId}")
    @Operation(
            summary = "Delete sub-subject",
            description = "Deletes a sub-subject by ID.",
            tags = {"SubSubject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "SubSubject not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteSubSubjectById(
            @PathVariable UUID subjectId,
            @PathVariable UUID subSubjectId
    ) {
        subSubjectService.deleteSubSubjectById(subjectId, subSubjectId);
        return buildResponse("SubSubject deleted", null, HttpStatus.OK);
    }


}
