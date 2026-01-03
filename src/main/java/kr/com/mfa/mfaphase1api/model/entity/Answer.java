package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.AnswerResponse;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID answerId;

    private String answerText;
    private Boolean isCorrect;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pointsAwarded;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_question"))
    @ToString.Exclude
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", foreignKey = @ForeignKey(name = "fk_answer_paper"))
    @ToString.Exclude
    private Paper paper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_submission"))
    @ToString.Exclude
    private Submission submission;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Annotation> annotations;

    public AnswerResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return AnswerResponse.builder()
                .answerId(this.answerId)
                .answerText(this.answerText)
                .isCorrect(this.isCorrect)
                .pointsAwarded(this.pointsAwarded)
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
                .questionId(this.question.getQuestionId())
                .paperId(this.paper != null ? this.paper.getPaperId() : null)
                .submissionId(this.submission.getSubmissionId())
                .build();
    }
}
