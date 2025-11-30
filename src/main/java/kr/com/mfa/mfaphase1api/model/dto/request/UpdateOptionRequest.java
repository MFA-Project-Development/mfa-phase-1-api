package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.Option;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOptionRequest {

    @NotNull
    private UUID optionId;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private Boolean isCorrect;

    public Option toEntity(Integer optionOrder, Question question){
        return Option.builder()
                .optionId(this.optionId)
                .text(this.text)
                .isCorrect(this.isCorrect)
                .optionOrder(optionOrder)
                .question(question)
                .build();
    }

}
