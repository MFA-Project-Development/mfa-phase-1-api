package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/assessments/{assessmentId}/submissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    @Operation(
            summary = "Start (or reuse) submission",
            description = "Creates a draft submission for the current student or returns the existing draft.",
            tags = {"Submission"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Submission ready",
                            content = @Content(schema = @Schema(implementation = SubmissionResponse.class)))
            }
    )
    public ResponseEntity<APIResponse<SubmissionResponse>> startSubmission(
            @PathVariable UUID assessmentId
    ) {
        submissionService.startSubmission(assessmentId);
        return buildResponse("Submission ready", null, HttpStatus.CREATED);
    }

}
