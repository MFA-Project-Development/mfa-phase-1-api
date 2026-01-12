package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
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
public class MotivationContentResponseWithExtraInfo {

    private UUID motivationContentId;
    private MotivationContentType type;
    private Boolean isDefault;
    private Object contentJson;
    private UUID createdBy;
    private Boolean isBookmarked;
    private Boolean isLiked;
    private Integer totalLikes;
    private Integer totalComments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
