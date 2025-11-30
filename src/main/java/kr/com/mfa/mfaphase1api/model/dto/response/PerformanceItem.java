package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformanceItem {
    private Month month;
    private Double score;
}
