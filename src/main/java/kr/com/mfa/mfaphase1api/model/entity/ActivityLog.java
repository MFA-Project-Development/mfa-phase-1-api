package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID activityLogId;
    private String actor;
    private String action;
    private String method;
    private String path;
    private Integer status;
    private Boolean success;
    private String ipAddress;
    private String userAgent;
    private String detail;
    private Instant createdAt;
    private String timeZone;

}
