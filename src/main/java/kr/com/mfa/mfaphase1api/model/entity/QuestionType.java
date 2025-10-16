package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionTypeResponse;
import lombok.*;

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
@Table(name = "question_types")
public class QuestionType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questionTypeId;

    @Column(nullable = false, unique = true)
    private String type;

    @ToString.Exclude
    @OneToMany(mappedBy = "questionType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    public QuestionTypeResponse toResponse() {
        return QuestionTypeResponse.builder()
                .questionTypeId(this.questionTypeId)
                .type(this.type)
                .build();
    }

}
