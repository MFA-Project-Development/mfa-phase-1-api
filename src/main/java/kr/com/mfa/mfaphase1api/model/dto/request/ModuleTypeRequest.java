package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.ModuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModuleTypeRequest {

    @NotNull
    @NotBlank
    private String type;

    public ModuleType toEntity(){
        return ModuleType.builder()
                .type(this.type)
                .build();
    }

    public ModuleType toEntity(UUID assessmentTypeId){
        return ModuleType.builder()
                .moduleTypeId(assessmentTypeId)
                .type(this.type)
                .build();
    }

}
