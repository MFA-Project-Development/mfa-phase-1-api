package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;

public interface DashboardService {
    AdminOverviewResponse getAdminOverview();
}
