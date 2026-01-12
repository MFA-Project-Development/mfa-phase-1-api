package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.entity.MotivationLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivationLikeRepository extends JpaRepository<MotivationLike, UUID> {
    Optional<MotivationLike> findByUserIdAndMotivationContent(UUID currentUserId, MotivationContent motivationContent);

    @Query("""
        SELECT l.motivationContent.motivationContentId
        FROM MotivationLike l
        WHERE l.userId = :userId
          AND l.motivationContent IN :contents
    """)
    List<UUID> findLikedContentIds(
            UUID userId,
            List<MotivationContent> contents
    );
}
