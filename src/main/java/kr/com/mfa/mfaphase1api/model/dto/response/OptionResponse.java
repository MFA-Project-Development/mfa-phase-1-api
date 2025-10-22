package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OptionResponse {

    private UUID optionId;
    private String text;
    private Boolean isCorrect;
    private Integer optionOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID questionId;

}
