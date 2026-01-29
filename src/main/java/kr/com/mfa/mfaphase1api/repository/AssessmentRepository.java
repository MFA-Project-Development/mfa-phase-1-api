package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    Optional<Assessment> findByAssessmentId_AndCreatedBy(UUID assessmentId, UUID createdBy);

    Optional<Assessment> findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(UUID assessmentId, UUID studentId);

    Page<Assessment> findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(UUID classId, Pageable pageable);

    Page<Assessment> findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndCreatedBy(UUID classId, UUID instructorId, Pageable pageable);

    Page<Assessment> findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(UUID classId, UUID studentId, Pageable pageable);

    Optional<Assessment> findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(UUID assessmentId, UUID classId);

    Optional<Assessment> findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(UUID assessmentId, UUID classId, UUID createdBy);

    Optional<Assessment> findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(UUID assessmentId, UUID classId, UUID studentId);

    boolean existsAssessmentsByAssessmentId_AndCreatedBy(UUID assessmentId, UUID createdBy);

    boolean existsAssessmentsByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(UUID assessmentId, UUID studentId);

    Page<Assessment> findAllByStatus(AssessmentStatus status, Pageable pageable);

    Page<Assessment> findAllByStatusAndCreatedBy(AssessmentStatus status, UUID createdBy, Pageable pageable);

    Page<Assessment> findAllByStatusAndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(AssessmentStatus status, UUID studentId, Pageable pageable);

    List<Assessment> findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId_AndStatus_AndStartDateBetween(UUID studentId, AssessmentStatus status, Instant startDate, Instant dueDate);

    List<Assessment> findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId_AndStatus(UUID studentId, AssessmentStatus status);

    List<Assessment> findAllByCreatedBy_AndStatus(UUID instructorId, AssessmentStatus status);

    List<Assessment> findAllByCreatedBy_AndStatus_AndStartDateBetween(UUID instructorId, AssessmentStatus status, Instant startDate, Instant dueDate);

    List<Assessment> findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(UUID currentUserId, AssessmentStatus assessmentStatus, UUID classId);

    List<Assessment> findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndStartDateBetween(UUID currentUserId, AssessmentStatus assessmentStatus, UUID classId, Instant startDate, Instant endDate);

    @Query("""
                select a
                from Assessment a
                where exists (
                    select 1 from Submission s
                    where s.assessment = a
                      and s.startedAt is not null
                )
                order by (
                    select max(s2.startedAt)
                    from Submission s2
                    where s2.assessment = a
                      and s2.startedAt is not null
                ) desc
            """)
    Page<Assessment> findRecentBySubmissionStartedAt(Pageable pageable);

    @Query("""
                select a
                from Assessment a
                where a.createdBy = :instructorId
                  and exists (
                    select 1 from Submission s
                    where s.assessment = a
                      and s.startedAt is not null
                  )
                order by (
                    select max(s2.startedAt)
                    from Submission s2
                    where s2.assessment = a
                      and s2.startedAt is not null
                ) desc
            """)
    Page<Assessment> findRecentBySubmissionStartedAtAndInstructor(UUID instructorId, Pageable pageable);

    @Query("""
                select a
                from Assessment a
                where exists (
                    select 1 from Submission s
                    where s.assessment = a
                      and s.studentId = :studentId
                      and s.startedAt is not null
                )
                order by (
                    select max(s2.startedAt)
                    from Submission s2
                    where s2.assessment = a
                      and s2.studentId = :studentId
                      and s2.startedAt is not null
                ) desc
            """)
    Page<Assessment> findRecentByMySubmissionStartedAt(UUID studentId, Pageable pageable);

    @Query("""
                select distinct a
                from Submission s
                join s.assessment a
                where s.studentId = :studentId
                  and a.startDate >= :start
                  and a.startDate < :endExclusive
                order by a.startDate desc
            """)
    List<Assessment> findAssessmentsInMonthByStudent(
            UUID studentId, Instant start, Instant endExclusive
    );


}
