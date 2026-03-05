package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentResponse {
    private UUID studentId;
    private String studentEmail;
    private String studentName;
    private String profileImage;
    private String loginEventType;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastLogoutTime;
    private String lastAction;
    private LocalDateTime lastActivityTime;
}
