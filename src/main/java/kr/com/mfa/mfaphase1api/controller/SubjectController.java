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
import kr.com.mfa.mfaphase1api.model.dto.request.SubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubjectResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubjectProperty;
import kr.com.mfa.mfaphase1api.service.SubjectService;
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
@RequestMapping("/api/v1/subjects")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class SubjectController {

    private final SubjectService subjectService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create subject",
            description = "Creates a new subject. Subject name must be unique (case-insensitive).",
            tags = {"Subject"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = SubjectResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Name already exists")
            }
    )
    public ResponseEntity<APIResponse<SubjectResponse>> createSubject(
            @RequestBody @Valid SubjectRequest request
    ) {
        return buildResponse("Subject created", subjectService.createSubject(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "List subjects",
            description = "Returns a paginated list of subjects with sorting.",
            tags = {"Subject"}
    )
    public ResponseEntity<APIResponse<PagedResponse<List<SubjectResponse>>>> getAllSubjects(
            @Parameter(description = "1-based page index", in = ParameterIn.QUERY, example = "1")
            @RequestParam(defaultValue = "1") @Positive Integer page,

            @Parameter(description = "Page size", in = ParameterIn.QUERY, example = "10")
            @RequestParam(defaultValue = "10") @Positive Integer size,

            @Parameter(description = "Sort property", in = ParameterIn.QUERY, example = "NAME")
            @RequestParam(required = false, defaultValue = "NAME") SubjectProperty property,

            @Parameter(description = "Sort direction", in = ParameterIn.QUERY, example = "ASC")
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction direction
    ) {
        return buildResponse(
                "Subjects retrieved",
                subjectService.getAllSubjects(page, size, property, direction),
                HttpStatus.OK
        );
    }

    @GetMapping("/{subject-id}")
    @Operation(
            summary = "Get subject",
            description = "Returns a subject by its ID.",
            tags = {"Subject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = SubjectResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Subject not found")
            }
    )
    public ResponseEntity<APIResponse<SubjectResponse>> getSubjectById(
            @PathVariable("subject-id") UUID subjectId
    ) {
        return buildResponse("Subject retrieved", subjectService.getSubjectById(subjectId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{subject-id}")
    @Operation(
            summary = "Update subject",
            description = "Updates a subject by ID. Subject name must remain unique.",
            tags = {"Subject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = SubjectResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Subject not found"),
                    @ApiResponse(responseCode = "409", description = "Name already exists")
            }
    )
    public ResponseEntity<APIResponse<SubjectResponse>> updateSubjectById(
            @PathVariable("subject-id") UUID subjectId,
            @RequestBody @Valid SubjectRequest request
    ) {
        return buildResponse("Subject updated", subjectService.updateSubjectById(subjectId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{subject-id}")
    @Operation(
            summary = "Delete subject",
            description = "Deletes a subject by ID.",
            tags = {"Subject"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Deleted"),
                    @ApiResponse(responseCode = "404", description = "Subject not found")
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteSubjectById(
            @PathVariable("subject-id") UUID subjectId
    ) {
        subjectService.deleteSubjectById(subjectId);
        return buildResponse("Subject deleted", null, HttpStatus.OK);
    }

}
