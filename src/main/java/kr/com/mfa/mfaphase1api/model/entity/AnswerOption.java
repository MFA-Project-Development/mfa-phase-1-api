package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "answer_options")
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID answerOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_option"))
    @ToString.Exclude
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false, foreignKey = @ForeignKey(name = "fk_option_answer_option"))
    @ToString.Exclude
    private Option option;

}
