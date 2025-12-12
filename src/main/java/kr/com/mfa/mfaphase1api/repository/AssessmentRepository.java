package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    Optional<Assessment> findByAssessmentId_AndCreatedBy(UUID assessmentId, UUID createdBy);;

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
}
