package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "motivation_s", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "motivation_content_id"})
})
public class MotivationLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationLikeId;

    @Column(nullable = false)
    private UUID studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivation_content_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motivation_bookmark_motivation_content"))
    @ToString.Exclude
    private MotivationContent motivationContent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private String timeZone;
}
