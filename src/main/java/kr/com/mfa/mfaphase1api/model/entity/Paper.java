package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.PaperResponse;
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
@Table(name = "papers")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paperId;

    @Column(nullable = false)
    private Integer page;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paper_submission"))
    @ToString.Exclude
    private Submission submission;

    public PaperResponse toResponse() {
        return PaperResponse.builder()
                .paperId(this.paperId)
                .page(this.page)
                .name(this.name)
                .submissionId(this.submission.getSubmissionId())
                .build();
    }

}
