package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Optional<Submission> findSubmissionByAssessmentAndStudentId(Assessment assessment, UUID studentId);

    Page<Submission> findAllByAssessment_AssessmentIdAndAssessment_CreatedBy(UUID assessmentId, UUID createdBy, Pageable pageable);

    Page<Submission> findAllByAssessment_AssessmentIdAndStudentId(UUID assessmentId, UUID studentId, Pageable pageable);

    Integer countByAssessment(Assessment assessment);

    Optional<Submission> findBySubmissionId_AndAssessment_AssessmentId(UUID submissionId, UUID assessmentId);

    Optional<Submission> findBySubmissionId_AndAssessment_AndStudentId(UUID submissionId, Assessment assessment, UUID studentId);

}
