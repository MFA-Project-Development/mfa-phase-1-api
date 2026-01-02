package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackResponse {

    private UUID feedbackId;
    private String comment;
    private UUID answerId;
    private UUID authorId;
    private UUID annotationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
