package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationBookmark;
import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivationBookmarkRepository extends JpaRepository<MotivationBookmark, UUID> {
    Optional<MotivationBookmark> findByStudentIdAndMotivationContent(UUID studentId, MotivationContent motivationContent);

    @Query("""
        SELECT b.motivationContent.motivationContentId
        FROM MotivationBookmark b
        WHERE b.studentId = :studentId
          AND b.motivationContent IN :contents
    """)
    List<UUID> findBookmarkedContentIds(
            UUID studentId,
            List<MotivationContent> contents
    );
}
