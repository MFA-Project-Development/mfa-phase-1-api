package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeachingSummary {
    private long assignments;
    private long homeworks;
    private long quizzes;
    private long exams;
}

