package kr.com.mfa.mfaphase1api.model.dto.request;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import kr.com.mfa.mfaphase1api.model.entity.QuestionType;
import kr.com.mfa.mfaphase1api.model.enums.GradingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionRequest {

    private String title;
    private String text;
    private BigDecimal points;
    private GradingMode mode;
    private UUID questionTypeId;

    public Question toEntity(Integer questionOrder, QuestionType questionType, Assessment assessment){
        return Question.builder()
                .text(this.text)
                .points(this.points)
                .mode(this.mode)
                .questionOrder(questionOrder)
                .questionType(questionType)
                .assessment(assessment)
                .build();
    }

}
