package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
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
    @Column(nullable = false)
    private AssessmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentType assessmentType;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assessment_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_assessment_type_assessment"))
//    @ToString.Exclude
//    private AssessmentType assessmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_sub_subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_sub_subject_assessment"))
    @ToString.Exclude
    private ClassSubSubjectInstructor classSubSubjectInstructor;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Resource> resources = new ArrayList<>();

    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public AssessmentResponse toResponse(){
        return AssessmentResponse.builder()
                .assessmentId(this.assessmentId)
                .title(this.title)
                .description(this.description)
                .startDate(this.startDate)
                .dueDate(this.dueDate)
                .timeLimit(this.timeLimit)
                .status(this.status)
                .assessmentType(this.assessmentType)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
//                .assessmentTypeId(this.assessmentType.getAssessmentTypeId())
                .subSubjectId(this.classSubSubjectInstructor.getClassSubSubject().getSubSubject().getSubSubjectId())
                .classId(this.classSubSubjectInstructor.getClassSubSubject().getClazz().getClassId())
                .createdBy(this.createdBy)
                .build();
    }

}
