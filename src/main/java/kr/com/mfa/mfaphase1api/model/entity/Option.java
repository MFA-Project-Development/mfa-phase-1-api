package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.OptionResponse;
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
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_option_question"))
    @ToString.Exclude
    private Question question;

    public OptionResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return OptionResponse.builder()
                .optionId(this.optionId)
                .text(this.text)
                .isCorrect(this.isCorrect)
                .optionOrder(this.optionOrder)
                .questionId(this.question.getQuestionId())
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
