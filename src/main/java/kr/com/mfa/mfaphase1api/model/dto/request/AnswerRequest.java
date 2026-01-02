package kr.com.mfa.mfaphase1api.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerRequest {

    @NotNull
    private BigDecimal pointsAwarded;

    @NotNull
    private UUID submissionId;

    @NotNull
    private UUID paperId;

}
