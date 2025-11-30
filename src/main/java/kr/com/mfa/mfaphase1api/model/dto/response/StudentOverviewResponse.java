package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.AverageStatus;
import kr.com.mfa.mfaphase1api.model.enums.ScoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentOverviewResponse {
    private Double score;
    private ScoreStatus scoreStatus;
    private Double progress;
    private Double progressChange;
    private Double average;
    private AverageStatus averageStatus;
    private List<PerformanceItem> performance;
}
