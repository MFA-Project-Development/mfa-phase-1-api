package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MotivationContentResponse {

    private UUID motivationContentId;
    private MotivationContentType type;
    private Boolean isDefault;
    private Object contentJson;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
