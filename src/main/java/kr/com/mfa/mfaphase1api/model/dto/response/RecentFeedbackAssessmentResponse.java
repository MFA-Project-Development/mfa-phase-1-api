package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentFeedbackAssessmentResponse {
    private UUID assessmentId;
    private UUID submissionId;
    private String title;
    private String gradedBy;
    private LocalDateTime publishedAt;
}
