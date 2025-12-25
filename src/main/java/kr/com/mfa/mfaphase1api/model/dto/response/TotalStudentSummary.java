package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TotalStudentSummary {
    private long total;
    private List<ClassStudentCount> byClass;
}
