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
public class InstructorAssessmentSummary {

    private long totalAssessments;
    private long totalAssessmentsInProgress;
    private long totalAssessmentsInReview;
    private long totalAssessmentPublished;
    private List<InstructorClassSummary> classes;

}
