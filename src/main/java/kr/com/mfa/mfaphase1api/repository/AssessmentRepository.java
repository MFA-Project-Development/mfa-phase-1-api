package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {


    Page<Assessment> findAllByCreatedBy(UUID createdBy, Pageable pageable);

    Optional<Assessment> findByAssessmentId_AndCreatedBy(UUID assessmentId, UUID createdBy);

    void deleteByAssessmentId_AndCreatedBy(UUID assessmentId, UUID createdBy);
}
