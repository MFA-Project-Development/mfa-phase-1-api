package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.entity.MotivationStudentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MotivationStudentLogRepository extends JpaRepository<MotivationStudentLog, UUID> {
}
