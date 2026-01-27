package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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

    Integer countByAssessmentAndPublishedAtIsNotNull(Assessment assessment);

    List<Submission> findAllByAssessment_AssessmentId(UUID assessmentId);

    List<Submission> findAllByStudentIdAndSubmittedAtIsNotNull(UUID studentId);

    @EntityGraph(attributePaths = {"answers"})
    List<Submission> findAllByStudentIdAndSubmittedAtIsNotNullAndPublishedAtIsNotNullAndSubmittedAtBetween(
            UUID studentId,
            Instant start,
            Instant end,
            Pageable pageable
    );

    Integer countByAssessmentAndStartedAtIsNotNull(Assessment assessment);

    Optional<Submission> findByAssessmentAndStudentId(Assessment assessment, UUID studentId);

    Optional<Submission> findFirstByStudentIdOrderBySubmittedAtDesc(UUID studentId);

    Optional<Submission> findFirstByStudentIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(
            UUID studentId,
            Instant monthStart,
            Instant monthEnd
    );

    @Query(value = """
                SELECT DISTINCT ON (DATE_TRUNC('month', submitted_at))
                       *
                FROM submissions
                WHERE student_id = :studentId
                  AND submitted_at IS NOT NULL
                ORDER BY DATE_TRUNC('month', submitted_at) DESC, submitted_at DESC
                LIMIT 2
            """, nativeQuery = true)
    List<Submission> findLastSubmissionOfLastTwoMonths(UUID studentId);

    @Query(value = """
                SELECT DISTINCT ON (DATE_TRUNC('month', submitted_at))
                       *
                FROM submissions
                WHERE student_id = :studentId
                  AND submitted_at IS NOT NULL
                  AND submitted_at < :endExclusive
                ORDER BY DATE_TRUNC('month', submitted_at) DESC, submitted_at DESC
                LIMIT 2
            """, nativeQuery = true)
    List<Submission> findLastSubmissionOfTwoMonthsEndingAt(
            UUID studentId,
            Instant endExclusive
    );

    @Query(value = """
                SELECT *
                FROM submissions
                WHERE student_id = :studentId
                  AND submitted_at IS NOT NULL
                  AND submitted_at >= :start
                  AND submitted_at < :endExclusive
                ORDER BY submitted_at DESC
            """, nativeQuery = true)
    List<Submission> findAllSubmissionsInMonth(
            UUID studentId,
            Instant start,
            Instant endExclusive
    );

}
