package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentResponseForGrading {

    private UUID assessmentId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private AssessmentType assessmentType;
    private String subSubjectName;
    private String className;
    private Integer totalSubmitted;
    private Integer totalStudents;
    private Boolean isPublished;

}
