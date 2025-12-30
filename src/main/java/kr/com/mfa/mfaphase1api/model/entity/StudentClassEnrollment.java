package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "student_class_enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "class_id"})
})
public class StudentClassEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID studentClassEnrollmentId;

    private Instant startDate;
    private Instant endDate;

    @Column(nullable = false)
    private String timeZone;

    private String moveReason;

    @Column(nullable = false)
    private UUID studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_student_class_enrollment_class"))
    @ToString.Exclude
    private Class clazz;

}
