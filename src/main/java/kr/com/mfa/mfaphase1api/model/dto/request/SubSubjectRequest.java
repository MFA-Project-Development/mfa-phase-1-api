package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import kr.com.mfa.mfaphase1api.model.entity.Subject;
import lombok.*;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubSubjectRequest {

    @NotNull
    @NotBlank
    private String name;

    public SubSubject toEntity(Subject subject){
        return SubSubject.builder()
                .name(this.name)
                .subject(subject)
                .build();
    }
}
