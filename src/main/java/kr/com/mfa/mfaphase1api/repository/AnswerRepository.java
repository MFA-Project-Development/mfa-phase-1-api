package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    Optional<Answer> findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(UUID answerId, UUID createdBy);

    Optional<Answer> findAnswersByQuestion_QuestionId_AndAnswerId(UUID questionId, UUID answerId);

    Optional<Answer> findAnswersByQuestion_QuestionId_AndAnswerId_AndQuestion_Assessment_CreatedBy(UUID questionId, UUID answerId, UUID createdBy);

    Optional<Answer> findAnswersByQuestion_QuestionId_AndAnswerId_AndSubmission_StudentId(UUID questionId, UUID answerId, UUID studentId);

    Page<Answer> findAllByQuestion_QuestionId(UUID questionId, Pageable pageable);

    Page<Answer> findAllByQuestion_QuestionId_AndQuestion_Assessment_CreatedBy(UUID questionId, UUID createdBy, Pageable pageable);

    Page<Answer> findAllByQuestion_QuestionId_AndSubmission_StudentId(UUID questionId, UUID studentId, Pageable pageable);

}
