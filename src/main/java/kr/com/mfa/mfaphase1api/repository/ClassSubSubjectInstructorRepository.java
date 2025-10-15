package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.entity.ClassSubSubject;
import kr.com.mfa.mfaphase1api.model.entity.ClassSubSubjectInstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassSubSubjectInstructorRepository extends JpaRepository<ClassSubSubjectInstructor, UUID> {

    boolean existsClassSubSubjectInstructorByClassSubSubject_ClassSubSubjectId_AndInstructorId(UUID classSubSubjectId, UUID instructorId);

    void deleteClassSubSubjectInstructorByClassSubSubject_AndInstructorId(ClassSubSubject classSubSubject, UUID instructorId);

    Optional<ClassSubSubjectInstructor> findClassSubSubjectInstructorByClassSubSubject_AndInstructorId(ClassSubSubject classSubSubject, UUID instructorId);

    Page<ClassSubSubjectInstructor> findDistinctByClassSubSubject_Clazz(Class clazz, Pageable pageable);

    long countDistinctByClassSubSubject_Clazz(Class clazz);




}
