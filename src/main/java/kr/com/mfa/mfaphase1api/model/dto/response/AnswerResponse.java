package kr.com.mfa.mfaphase1api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AnswerResponse {

    private UUID answerId;
    private String answerText;
    private Boolean isCorrect;
    private BigDecimal pointsAwarded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID questionId;
    private UUID paperId;
    private UUID submissionId;


}
