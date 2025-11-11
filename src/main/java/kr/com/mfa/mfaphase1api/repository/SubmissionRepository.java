package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    boolean existsByAssessment_AssessmentIdAndStudentId(UUID assessmentId, UUID studentId);

    Optional<Submission> findSubmissionByAssessmentAndStudentId(Assessment assessment, UUID studentId);
}
