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
public class StudentResponseResultSummary {
    private BigDecimal maxScore;
    private BigDecimal scoreEarned;
    private Long totalCorrect;
    private Long totalIncorrect;
    private Long totalFeedbacks;
}
