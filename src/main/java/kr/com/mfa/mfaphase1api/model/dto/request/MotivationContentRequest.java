package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MotivationContentRequest {

    @NotNull
    private MotivationContentType type;

    @NotNull
    private boolean isDefault;

    @NotNull
    private Object contentJson;

    public MotivationContent toEntity(UUID userId){
        return MotivationContent.builder()
                .type(this.type)
                .isDefault(this.isDefault)
                .contentJson(this.contentJson)
                .createdBy(userId)
                .build();
    }
}
