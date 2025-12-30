package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.*;
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
    private Instant startDate;
    private Instant dueDate;

    private String timeZone;

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
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public AssessmentResponse toResponse(Integer totalSubmitted) {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return AssessmentResponse.builder()
                .assessmentId(this.assessmentId)
                .title(this.title)
                .description(this.description)
                .startDate(
                        this.startDate != null
                                ? LocalDateTime.ofInstant(this.startDate, zone)
                                : null
                )
                .dueDate(
                        this.dueDate != null
                                ? LocalDateTime.ofInstant(this.dueDate, zone)
                                : null
                )
                .timeZone(zone.getId())
                .timeLimit(this.timeLimit)
                .status(this.status)
                .assessmentType(this.assessmentType)
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
                .subSubjectId(
                        this.classSubSubjectInstructor
                                .getClassSubSubject()
                                .getSubSubject()
                                .getSubSubjectId()
                )
                .classId(
                        this.classSubSubjectInstructor
                                .getClassSubSubject()
                                .getClazz()
                                .getClassId()
                )
                .createdBy(this.createdBy)
                .totalSubmitted(totalSubmitted)
                .build();
    }

}
