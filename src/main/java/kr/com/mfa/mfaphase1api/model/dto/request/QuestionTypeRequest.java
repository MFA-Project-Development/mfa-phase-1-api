package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionTypeRequest {

    @NotNull
    @NotBlank
    private String type;

    public QuestionType toEntity(){
        return QuestionType.builder()
                .type(this.type)
                .build();
    }

    public QuestionType toEntity(UUID questionTypeId){
        return QuestionType.builder()
                .questionTypeId(questionTypeId)
                .type(this.type)
                .build();
    }

}
