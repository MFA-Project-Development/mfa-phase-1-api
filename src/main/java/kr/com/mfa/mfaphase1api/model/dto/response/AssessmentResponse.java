package kr.com.mfa.mfaphase1api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentResponse {

    private UUID assessmentId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private String timeZone;
    private Integer timeLimit;
    private AssessmentStatus status;
    private AssessmentType assessmentType;
//    private UUID assessmentTypeId;
    private UUID subSubjectId;
    private UUID classId;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer totalSubmitted;

}
