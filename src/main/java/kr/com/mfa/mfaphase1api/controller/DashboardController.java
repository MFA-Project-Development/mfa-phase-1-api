package kr.com.mfa.mfaphase1api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.ClassSummaryResponse;
import kr.com.mfa.mfaphase1api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return buildResponse("Dashboard overview retrieved", dashboardService.getAdminOverview(), HttpStatus.OK);
    }

}
