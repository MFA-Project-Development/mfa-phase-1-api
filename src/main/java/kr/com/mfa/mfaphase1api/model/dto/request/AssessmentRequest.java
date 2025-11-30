package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.ClassSubSubjectInstructor;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentRequest {

    @NotNull
    @NotBlank
    private String title;
    private String description;

    @NotNull
    private Integer timeLimit;

    @NotNull
    private AssessmentType assessmentType;

//    @NotNull
//    private UUID assessmentTypeId;

    public Assessment toEntity(UUID userId, ClassSubSubjectInstructor classSubSubjectInstructor){
        return Assessment.builder()
                .title(this.title)
                .description(this.description)
                .timeLimit(this.timeLimit)
                .status(AssessmentStatus.DRAFTED)
                .assessmentType(this.assessmentType)
                .classSubSubjectInstructor(classSubSubjectInstructor)
                .createdBy(userId)
                .build();
    }

}
