package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentMessage {

    private String assessmentId;
    private String title;
    private String description;
    private String startDate;
    private String dueDate;
    private String timeLimit;
    private String status;
    private String assessmentType;

}
