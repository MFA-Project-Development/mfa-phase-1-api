package kr.com.mfa.mfaphase1api.repository;

import jakarta.transaction.Transactional;
import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.entity.ClassSubSubject;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassSubSubjectRepository extends JpaRepository<ClassSubSubject, UUID> {

    boolean existsClassSubSubjectByClazz_ClassId_AndSubSubject_SubSubjectId(UUID classId, UUID subSubjectId);

    @Transactional
    void deleteClassSubSubjectByClazz_AndSubSubject(Class clazz, SubSubject subSubject);

    Optional<ClassSubSubject> findClassSubSubjectByClazz_AndSubSubject(Class clazz, SubSubject subSubject);

    Page<ClassSubSubject> findAllByClazz(Class clazz, Pageable pageable);

    long countByClazz(Class clazz);
}
