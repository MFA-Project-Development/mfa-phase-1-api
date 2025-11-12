package kr.com.mfa.mfaphase1api.model.dto.request;

import kr.com.mfa.mfaphase1api.model.entity.Class;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassRequest {
    private String name;

    public Class toEntity(String code){
        return Class.builder()
                .name(this.name)
                .code(code)
                .build();
    }

    public Class toEntity(UUID classId, String code){
        return Class.builder()
                .classId(classId)
                .name(this.name)
                .code(code)
                .build();
    }
}
