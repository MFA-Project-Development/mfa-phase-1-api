package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivationCommentRepository extends JpaRepository<MotivationComment, UUID> {
    Page<MotivationComment> findAllByMotivationContent_MotivationContentId(UUID motivationContentId, Pageable pageable);

    Optional<MotivationComment> findByMotivationCommentId_AndStudentId_AndMotivationContent_MotivationContentId(UUID motivationCommentId, UUID studentId, UUID motivationContentId);
}
