package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstructorResponse {
    private UUID instructorId;
    private String instructorEmail;
    private String instructorName;
    private SubSubjectResponse subSubjectResponse;
    private String profileImage;
}
