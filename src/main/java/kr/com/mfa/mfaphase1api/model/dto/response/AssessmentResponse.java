package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
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
public class AssessmentResponse {

    private UUID assessmentId;
    private String title;
    private String description;
    private Integer timeLimit;
    private AssessmentStatus status;
    private UUID assessmentTypeId;
    private UUID subSubjectId;
    private UUID classId;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
