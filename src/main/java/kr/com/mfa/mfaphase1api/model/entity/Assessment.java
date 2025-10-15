package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assessmentId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer timeLimit;

    @Enumerated(EnumType.STRING)
    private AssessmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_assessment_type_assessment"))
    @ToString.Exclude
    private AssessmentType assessmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_sub_subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_sub_subject_assessment"))
    @ToString.Exclude
    private ClassSubSubjectInstructor classSubSubjectInstructor;

    private UUID createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public AssessmentResponse toResponse(UserResponse userResponse, AssessmentTypeResponse assessmentTypeResponse, SubSubjectResponse subSubjectResponse, ClassResponse classResponse){
        return AssessmentResponse.builder()
                .assessmentId(this.assessmentId)
                .title(this.title)
                .description(this.description)
                .timeLimit(this.timeLimit)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .assessmentTypeResponse(assessmentTypeResponse)
                .subSubjectResponse(subSubjectResponse)
                .classResponse(classResponse)
                .createdBy(userResponse)
                .build();
    }

}
