package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionTypeResponse;
import kr.com.mfa.mfaphase1api.model.enums.GradingMode;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_question_type"))
    @ToString.Exclude
    private QuestionType questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_assessment"))
    @ToString.Exclude
    private Assessment assessment;

    public QuestionResponse toResponse(QuestionTypeResponse questionTypeResponse, AssessmentResponse assessmentResponse) {
        return QuestionResponse.builder()
                .questionId(this.questionId)
                .text(this.text)
                .points(this.points)
                .mode(this.mode)
                .questionOrder(this.questionOrder)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .questionTypeResponse(questionTypeResponse)
                .assessmentResponse(assessmentResponse)
                .build();
    }
}
