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
public class ClassSummaryResponse {

    private UUID classId;
    private String className;
    private long totalSubSubjects;
    private long totalStudents;
    private long totalInstructors;

}
