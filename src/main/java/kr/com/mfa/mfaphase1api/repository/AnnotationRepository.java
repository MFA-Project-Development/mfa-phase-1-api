package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Annotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, UUID> {

    Optional<Annotation> findAnnotationByAnswer_AnswerId_AndAnnotationId(UUID answerId, UUID annotationId);

    Optional<Annotation> findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Question_Assessment_CreatedBy(UUID answerId, UUID annotationId, UUID authorId);

    Optional<Annotation> findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Submission_StudentId(UUID answerId, UUID annotationId, UUID studentId);

    Page<Annotation> findAllByAnswer_AnswerId(UUID answerId, Pageable pageable);

    Page<Annotation> findAllByAnswer_AnswerId_AndAnswer_Submission_StudentId(UUID answerId, UUID studentId, Pageable pageable);

    Page<Annotation> findAllByAnswer_AnswerId_AndAnswer_Question_Assessment_CreatedBy(UUID answerId, UUID authorId, Pageable pageable);

}
