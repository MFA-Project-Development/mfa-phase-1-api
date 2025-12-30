package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentSummaryByClass {
    private UUID classId;
    private String className;
    private long assignments;
    private long homeworks;
    private long quizzes;
    private long exams;
}

