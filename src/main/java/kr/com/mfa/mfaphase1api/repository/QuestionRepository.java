package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    int countByAssessment(Assessment assessment);

    Optional<Question> findByAssessment_AssessmentId_AndQuestionId(UUID assessmentId, UUID questionId);

    Page<Question> findAllByAssessment_AssessmentId(UUID assessmentId, Pageable pageable);

    Optional<Question> findByQuestionId_AndAssessment_CreatedBy(UUID questionId, UUID instructorId);

    Optional<Question> findByQuestionId_AndAssessment_ClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(UUID questionId, UUID studentId);
}
