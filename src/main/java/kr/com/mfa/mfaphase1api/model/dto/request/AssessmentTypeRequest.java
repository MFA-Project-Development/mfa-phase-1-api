package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssessmentTypeRequest {

    @NotNull
    @NotBlank
    private String type;

    public AssessmentType toEntity(){
        return AssessmentType.builder()
                .type(this.type)
                .build();
    }

    public AssessmentType toEntity(UUID assessmentTypeId){
        return AssessmentType.builder()
                .assessmentTypeId(assessmentTypeId)
                .type(this.type)
                .build();
    }

}
