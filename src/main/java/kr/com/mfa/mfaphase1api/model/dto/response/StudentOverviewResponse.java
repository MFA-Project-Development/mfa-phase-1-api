package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.AverageStatus;
import kr.com.mfa.mfaphase1api.model.enums.ScoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentOverviewResponse {
    private BigDecimal score;
    private ScoreStatus scoreStatus;
    private BigDecimal progressPercent;
    private BigDecimal progressChange;
    private BigDecimal average;
    private AverageStatus averageStatus;
    private List<PerformanceItem> performance;
}
