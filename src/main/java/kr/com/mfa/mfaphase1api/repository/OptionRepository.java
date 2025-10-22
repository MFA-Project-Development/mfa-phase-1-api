package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Option;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptionRepository extends JpaRepository<Option, UUID> {
    int countByQuestion(Question question);

    Page<Option> findAllByQuestion_QuestionId(UUID questionId, Pageable pageable);

    Optional<Option> findAllByQuestion_QuestionId_AndOptionId(UUID questionQuestionId, UUID optionId);
}
