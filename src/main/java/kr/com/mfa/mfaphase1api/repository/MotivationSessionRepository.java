package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.entity.MotivationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MotivationSessionRepository extends JpaRepository<MotivationSession, UUID> {
}
