package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionResponse {
    private UUID submissionId;
    private UUID assessmentId;
    private StudentResponse studentResponse;
    private UUID gradedBy;
    private SubmissionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Boolean isGraded;
    private LocalDateTime gradedAt;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private BigDecimal maxScore;
    private BigDecimal scoreEarned;
}
