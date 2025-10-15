package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubSubjectResponse {

    private UUID subSubjectId;
    private String name;
    private SubjectResponse subjectResponse;

}
