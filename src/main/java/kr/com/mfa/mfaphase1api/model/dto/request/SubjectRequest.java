package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.entity.Subject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectRequest {

    @NotNull
    @NotBlank
    private String name;

    public Subject toEntity(){
        return Subject.builder()
                .name(this.name)
                .build();
    }

    public Subject toEntity(UUID subjectId){
        return Subject.builder()
                .name(this.name)
                .build();
    }

}
