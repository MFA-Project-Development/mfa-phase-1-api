package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentSummaryByClass {
    private UUID classId;
    private String className;
    private BigDecimal assignments;
    private BigDecimal homeworks;
    private BigDecimal quizzes;
    private BigDecimal exams;
}

