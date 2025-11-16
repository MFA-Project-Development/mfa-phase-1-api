package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {
}
