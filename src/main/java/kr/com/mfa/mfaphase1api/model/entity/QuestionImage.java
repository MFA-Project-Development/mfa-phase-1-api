package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionImageResponse;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "question_images")
public class QuestionImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID questionImageId;

    @Column(nullable = false)
    private Integer imageOrder;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_image_question"))
    @ToString.Exclude
    private Question question;

    public QuestionImageResponse toResponse(){
        return QuestionImageResponse.builder()
                .questionImageId(this.questionImageId)
                .imageOrder(this.imageOrder)
                .imageUrl(this.imageUrl)
                .build();
    }
}
