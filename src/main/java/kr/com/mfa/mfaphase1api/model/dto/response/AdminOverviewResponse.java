package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminOverviewResponse {

    private long totalInstructors;
    private long totalSubSubjects;
    private long totalClasses;
    private long totalStudents;

}
