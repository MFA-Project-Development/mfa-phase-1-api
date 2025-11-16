package kr.com.mfa.mfaphase1api.model.dto.response;

import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceResponse {
    private UUID resourceId;
    private ResourceKind kind;
    private String title;
    private String name;
    private UUID assessmentId;
}
