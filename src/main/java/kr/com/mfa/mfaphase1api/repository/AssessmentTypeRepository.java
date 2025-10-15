package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssessmentTypeRepository extends JpaRepository<AssessmentType, UUID> {

    boolean existsByTypeIgnoreCase(String type);


}
