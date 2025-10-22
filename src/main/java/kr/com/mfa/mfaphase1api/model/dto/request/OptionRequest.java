package kr.com.mfa.mfaphase1api.model.dto.request;

import kr.com.mfa.mfaphase1api.model.entity.Option;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionRequest {

    private String text;
    private Boolean isCorrect;

    public Option toEntity(Integer optionOrder, Question question){
        return Option.builder()
                .text(this.text)
                .isCorrect(this.isCorrect)
                .optionOrder(optionOrder)
                .question(question)
                .build();
    }

}
