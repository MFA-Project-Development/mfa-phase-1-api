package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubSubjectRepository extends JpaRepository<SubSubject, UUID> {
    boolean existsByNameIgnoreCaseAndSubject_SubjectId(String name, UUID subjectId);

    boolean existsByNameIgnoreCaseAndSubject_SubjectIdAndSubSubjectIdNot(String name, UUID subjectId, UUID excludeId);

    Optional<SubSubject> findByClassSubSubjects_Clazz_ClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(UUID classId, UUID instructorId);

    Optional<SubSubject> findByClassSubSubjects_Clazz_ClassId(UUID classId);
}
