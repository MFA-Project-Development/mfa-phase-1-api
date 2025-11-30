package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentOverviewResponse;

import java.time.Month;

public interface DashboardService {
    AdminOverviewResponse getAdminOverview();

    StudentOverviewResponse getStudentOverview(Month month, String subSubjectName);
}
