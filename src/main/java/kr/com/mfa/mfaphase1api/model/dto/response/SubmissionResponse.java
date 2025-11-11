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
    private UUID studentId;
    private UUID gradedBy;
    private SubmissionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private BigDecimal maxScore;
    private BigDecimal scoreEarned;
}
