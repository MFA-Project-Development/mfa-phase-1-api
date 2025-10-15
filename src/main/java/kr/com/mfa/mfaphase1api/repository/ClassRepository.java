package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassRepository extends JpaRepository<Class, UUID> {
    boolean existsByNameIgnoreCase(String name);

    Page<Class> findAllByClassSubSubjects_ClassSubSubjectInstructors_InstructorId(UUID instructorId, Pageable pageable);

    Page<Class> findAllByStudentClassEnrollments_StudentId(UUID studentId, Pageable pageable);

    Optional<Class> findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(UUID classId, UUID instructorId);

    Optional<Class> findByClassId_AndStudentClassEnrollments_StudentId(UUID classId, UUID studentId);
}
