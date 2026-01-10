package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationCommentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentResponse;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "motivation_comments")
public class MotivationComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationCommentId;

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivation_content_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motivation_bookmark_motivation_content"))
    @ToString.Exclude
    private MotivationContent motivationContent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

    public MotivationCommentResponse toResponse(StudentResponse studentResponse) {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return MotivationCommentResponse.builder()
                .motivationCommentId(this.motivationCommentId)
                .comment(this.comment)
                .studentResponse(studentResponse)
                .motivationContentId(this.motivationContent.getMotivationContentId())
                .createdAt(
                        this.createdAt != null
                                ? LocalDateTime.ofInstant(this.createdAt, zone)
                                : null
                )
                .updatedAt(
                        this.updatedAt != null
                                ? LocalDateTime.ofInstant(this.updatedAt, zone)
                                : null
                )
                .build();
    }
}
