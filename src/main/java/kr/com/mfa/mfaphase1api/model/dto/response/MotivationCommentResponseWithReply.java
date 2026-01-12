package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MotivationCommentResponseWithReply {
    private UUID motivationCommentId;
    private String comment;
    private UserResponse userResponse;
    private UUID motivationContentId;
    private List<MotivationCommentResponseWithReply> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
