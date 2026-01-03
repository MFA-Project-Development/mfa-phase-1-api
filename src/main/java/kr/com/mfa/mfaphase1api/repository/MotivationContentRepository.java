package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivationContentRepository extends JpaRepository<MotivationContent, UUID> {

    Page<MotivationContent> findAllByTypeOrIsDefault(MotivationContentType type, Boolean isDefault,
                                                                Pageable pageable);

    Optional<MotivationContent> findByCreatedByAndMotivationContentId(UUID createdBy, UUID motivationContentId);

}
