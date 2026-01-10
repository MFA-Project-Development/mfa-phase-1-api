package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.enums.GradingMode;
import kr.com.mfa.mfaphase1api.model.enums.QuestionType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questionId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal points;

    @Column(nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradingMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "question_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_question_type"))
//    @ToString.Exclude
//    private QuestionType questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_assessment"))
    @ToString.Exclude
    private Assessment assessment;

    @ToString.Exclude
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Option> options = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuestionImage> questionImages = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Answer> answers = new ArrayList<>();

    public QuestionResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return QuestionResponse.builder()
                .questionId(this.questionId)
                .text(this.text)
                .points(this.points)
                .mode(this.mode)
                .questionType(this.questionType)
                .questionOrder(this.questionOrder)
                .questionImagesResponse(this.questionImages != null
                        ? this.questionImages.stream().map(QuestionImage::toResponse).toList()
                        : null)
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
//                .questionTypeId(this.questionType.getQuestionTypeId())
                .assessmentId(this.assessment.getAssessmentId())
                .build();
    }
}
