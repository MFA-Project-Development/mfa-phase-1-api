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
    private String code;

    public Class toEntity(){
        return Class.builder()
                .name(this.name)
                .code(this.code)
                .build();
    }

    public Class toEntity(UUID classId){
        return Class.builder()
                .classId(classId)
                .name(this.name)
                .build();
    }
}
