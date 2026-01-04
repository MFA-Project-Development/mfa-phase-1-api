package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import kr.com.mfa.mfaphase1api.model.enums.GradingMode;
import kr.com.mfa.mfaphase1api.model.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateQuestionRequest {

    @NotNull
    private UUID questionId;

    @NotNull
    @NotBlank
    private String title;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private BigDecimal points;

    @NotNull
    private GradingMode mode;

    @NotNull
    private QuestionType questionType;
//    private UUID questionTypeId;

    private List<String> questionImages;

    public Question toEntity(Integer questionOrder, Assessment assessment){
        return Question.builder()
                .questionId(this.questionId)
                .text(this.text)
                .points(this.points)
                .mode(this.mode)
                .questionType(this.questionType)
                .questionOrder(questionOrder)
                .assessment(assessment)
                .build();
    }

}
