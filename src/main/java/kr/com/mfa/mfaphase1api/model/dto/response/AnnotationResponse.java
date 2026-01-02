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
public class AnnotationResponse {

    private UUID annotationId;
    private Object contentJson;
    private UUID answerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
