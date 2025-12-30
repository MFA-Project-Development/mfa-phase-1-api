package kr.com.mfa.mfaphase1api.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "motivation_student_logs")
public class MotivationStudentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationStudentLogId;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Object deliveryJson;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Object engagementJson;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Object effectivenessJson;

    @Column(nullable = false)
    private UUID studentId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivation_session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motivation_student_log_motivation_session"))
    @ToString.Exclude
    private MotivationSession motivationSession;

}
