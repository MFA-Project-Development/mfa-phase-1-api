package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.ClassSubSubjectInstructor;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate startDate;
    private LocalDate dueDate;

    @NotNull
    private Integer timeLimit;

    @NotNull
    private AssessmentStatus status;

    @NotNull
    private AssessmentType assessmentType;

//    @NotNull
//    private UUID assessmentTypeId;

    public Assessment toEntity(UUID userId, ClassSubSubjectInstructor classSubSubjectInstructor){
        return Assessment.builder()
                .title(this.title)
                .description(this.description)
                .startDate(this.startDate)
                .dueDate(this.dueDate)
                .timeLimit(this.timeLimit)
                .status(this.status)
                .assessmentType(this.assessmentType)
                .classSubSubjectInstructor(classSubSubjectInstructor)
                .createdBy(userId)
                .build();
    }

}
