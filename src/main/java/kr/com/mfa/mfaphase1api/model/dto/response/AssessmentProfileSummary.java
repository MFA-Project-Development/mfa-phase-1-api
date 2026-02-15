package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentProfileSummary {
    private Long totalAssessments;
    private BigDecimal percentageAssessments;
    private Long totalCompleted;
    private BigDecimal percentageCompleted;
    private Long totalPending;
    private BigDecimal percentagePending;
    private Long totalFailed;
    private BigDecimal percentageFailed;
}
