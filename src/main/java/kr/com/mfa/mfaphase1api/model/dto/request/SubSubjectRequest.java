package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255)
    private String name;

    @NotNull
    private UUID subjectId;

    public SubSubject toEntity(Subject subject){
        return SubSubject.builder()
                .name(this.name)
                .subject(subject)
                .build();
    }

    public SubSubject toEntity(UUID subSubjectId, Subject subject){
        return SubSubject.builder()
                .subSubjectId(subSubjectId)
                .name(this.name)
                .subject(subject)
                .build();
    }
}
