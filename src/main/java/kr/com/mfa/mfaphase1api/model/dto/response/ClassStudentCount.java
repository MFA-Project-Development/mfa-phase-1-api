package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassStudentCount {
    private UUID classId;
    private String className;
    private long studentCount;
    private double percentage;
}

