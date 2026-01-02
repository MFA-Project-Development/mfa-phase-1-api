package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.FeedbackResponse;
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
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID feedbackId;

    @Column(nullable = false)
    private String comment;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feedback_answer"))
    @ToString.Exclude
    private Answer answer;

    @Column(nullable = false)
    private UUID authorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_feedback_annotation"))
    @ToString.Exclude
    private Annotation annotation;

    public FeedbackResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return FeedbackResponse.builder()
                .feedbackId(this.feedbackId)
                .comment(this.comment)
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
                .authorId(this.authorId)
                .answerId(this.answer.getAnswerId())
                .annotationId(this.annotation.getAnnotationId())
                .build();
    }

}
