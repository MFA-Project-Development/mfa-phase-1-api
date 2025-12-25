package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentOverviewResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.TeacherOverviewResponse;

import java.time.Month;
import java.util.UUID;

public interface DashboardService {
    AdminOverviewResponse getAdminOverview();

    StudentOverviewResponse getStudentOverview(Month month, String subSubjectName);

    TeacherOverviewResponse getInstructorOverview(Month month, UUID classId);
}
