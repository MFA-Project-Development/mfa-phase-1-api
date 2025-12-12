package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Table(name = "submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assessment_id", "student_id"})
})
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID submissionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxScore;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal scoreEarned;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;

    @Column(nullable = false)
    private UUID studentId;

    private UUID gradedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submission_assessment"))
    @ToString.Exclude
    private Assessment assessment;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Paper> papers = new ArrayList<>();

    public SubmissionResponse toResponse(StudentResponse studentResponse){
        return SubmissionResponse.builder()
                .submissionId(this.submissionId)
                .status(this.status)
                .maxScore(this.maxScore)
                .scoreEarned(this.scoreEarned)
                .startedAt(this.startedAt)
                .submittedAt(this.submittedAt)
                .gradedAt(this.gradedAt)
                .studentResponse(studentResponse)
                .gradedBy(this.gradedBy)
                .assessmentId(this.assessment.getAssessmentId())
                .build();
    }

}
