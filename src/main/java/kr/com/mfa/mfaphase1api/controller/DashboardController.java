package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.ClassSummaryResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentOverviewResponse;
import kr.com.mfa.mfaphase1api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.buildResponse;

@RestController
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
@SecurityRequirement(name = "mfa")
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/overview")
    @Operation(
            summary = "Get admin dashboard overview",
            description = "Returns aggregate counts for admin dashboard: subjects, students, and instructors.",
            tags = {"Dashboard"}
    )
    public ResponseEntity<APIResponse<AdminOverviewResponse>> getAdminOverview(
    ) {
        return buildResponse("Admin dashboard overview retrieved", dashboardService.getAdminOverview(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/overview")
    @Operation(
            summary = "Get student dashboard overview",
            description = "Returns aggregate counts for student dashboard: subjects, students, and instructors.",
            tags = {"Dashboard"}
    )
    public ResponseEntity<APIResponse<StudentOverviewResponse>> getStudentOverview(
            @RequestParam(required = false) Month month,
            @RequestParam(required = false) String subSubjectName
    ) {
        return buildResponse("Student dashboard overview retrieved", dashboardService.getStudentOverview(month, subSubjectName), HttpStatus.OK);
    }

}
