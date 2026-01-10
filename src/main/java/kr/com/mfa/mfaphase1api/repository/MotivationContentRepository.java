package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivationContentRepository extends JpaRepository<MotivationContent, UUID> {

    Page<MotivationContent> findAllByTypeOrIsDefault(MotivationContentType type, Boolean isDefault,
                                                                Pageable pageable);

    Optional<MotivationContent> findByCreatedByAndMotivationContentId(UUID createdBy, UUID motivationContentId);


    @Query("""
        SELECT m
        FROM MotivationContent m
        WHERE (:type IS NULL OR m.type = :type)
          AND (:createdBy IS NULL OR m.createdBy = :createdBy)
          AND (:isDefault IS NULL OR m.isDefault = :isDefault)
    """)
    Page<MotivationContent> search(
            MotivationContentType type, UUID createdBy, Boolean isDefault, Pageable pageable
    );

    @Query("""
        SELECT m
        FROM MotivationBookmark b
        JOIN b.motivationContent m
        WHERE b.studentId = :studentId
          AND (:type IS NULL OR m.type = :type)
          AND (:createdBy IS NULL OR m.createdBy = :createdBy)
          AND (:isDefault IS NULL OR m.isDefault = :isDefault)
    """)
    Page<MotivationContent> searchBookmarked(
            UUID studentId,
            MotivationContentType type,
            UUID createdBy,
            Boolean isDefault,
            Pageable pageable
    );

    @Query("""
        SELECT m
        FROM MotivationContent m
        WHERE NOT EXISTS (
            SELECT 1
            FROM MotivationBookmark b
            WHERE b.studentId = :studentId
              AND b.motivationContent = m
        )
          AND (:type IS NULL OR m.type = :type)
          AND (:createdBy IS NULL OR m.createdBy = :createdBy)
          AND (:isDefault IS NULL OR m.isDefault = :isDefault)
    """)
    Page<MotivationContent> searchNotBookmarked(
            UUID studentId,
            MotivationContentType type,
            UUID createdBy,
            Boolean isDefault,
            Pageable pageable
    );

}
