package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Optional<Feedback> findFeedbackByAnswer_AnswerIdAndFeedbackId(UUID answerId, UUID feedbackId);

    Optional<Feedback> findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAuthorId(UUID answerId, UUID feedbackId, UUID authorId);

    Optional<Feedback> findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAnswer_Submission_StudentId(UUID answerId, UUID feedbackId, UUID studentId);

    Page<Feedback> findAllByAnswer_AnswerId(UUID answerId, Pageable pageable);

    Page<Feedback> findAllByAnswer_AnswerId_AndAuthorId(UUID answerId, UUID authorId, Pageable pageable);

    Page<Feedback> findAllByAnswer_AnswerId_AndAnswer_Submission_StudentId(UUID answerId, UUID studentId, Pageable pageable);

    Optional<Feedback> findFeedbackByAnswer_AnswerId_AndAnnotation_AnnotationId(UUID answerId, UUID annotationId);

    @Query("""
            select count(f)
            from Feedback f
            join f.answer a
            join a.submission s
            where s.submissionId = :submissionId
            """)
    long countFeedbacksBySubmissionId(UUID submissionId);

}
