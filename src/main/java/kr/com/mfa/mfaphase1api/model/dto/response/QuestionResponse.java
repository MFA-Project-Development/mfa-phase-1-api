package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.GradingMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionResponse {

    private UUID questionId;
    private String text;
    private BigDecimal points;
    private Integer questionOrder;
    private GradingMode mode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID questionTypeId;
    private UUID assessmentId;

}
