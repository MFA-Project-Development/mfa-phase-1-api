package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.OptionResponse;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID optionId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Boolean isCorrect;

    @Column(nullable = false)
    private Integer optionOrder;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_option_question"))
    @ToString.Exclude
    private Question question;

    public OptionResponse toResponse() {
        return OptionResponse.builder()
                .optionId(this.optionId)
                .text(this.text)
                .isCorrect(this.isCorrect)
                .optionOrder(this.optionOrder)
                .questionId(this.question.getQuestionId())
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

}
