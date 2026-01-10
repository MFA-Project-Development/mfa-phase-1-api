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
public class MotivationCommentResponse {
    private UUID motivationCommentId;
    private String comment;
    private StudentResponse studentResponse;
    private UUID motivationContentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
