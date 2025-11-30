package kr.com.mfa.mfaphase1api.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentScheduleRequest {
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}
