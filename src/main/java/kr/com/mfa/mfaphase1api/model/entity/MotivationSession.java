package kr.com.mfa.mfaphase1api.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "motivation_sessions")
public class MotivationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationSessionId;

    @Column(nullable = false)
    private String context;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Object deliveryJson;

    @Column(nullable = false)
    private UUID instructorId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivation_content_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motivation_session_motivation_content"))
    @ToString.Exclude
    private MotivationContent motivationContent;

    @ToString.Exclude
    @OneToMany(mappedBy = "motivationSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MotivationStudentLog> motivationStudentLogs = new ArrayList<>();


}
