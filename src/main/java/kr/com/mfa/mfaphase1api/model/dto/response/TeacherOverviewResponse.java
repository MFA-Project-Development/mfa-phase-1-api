package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherOverviewResponse {
    private TotalStudentSummary totalStudentSummary;
    private TeachingSummary teachingSummary;
    private List<AssessmentSummaryByClass> avgStudentScoreByClasses;
    private RecentActivity recentActivity;

}
