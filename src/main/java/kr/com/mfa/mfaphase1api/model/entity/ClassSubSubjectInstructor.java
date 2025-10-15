package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
@Table(name = "class_sub_subject_instructors", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_sub_subject_id", "instructor_id"})
})
public class ClassSubSubjectInstructor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID classSubSubjectInstructorId;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private UUID instructorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_sub_subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_sub_subject_instructor_sub_subject"))
    @ToString.Exclude
    private ClassSubSubject classSubSubject;

}
