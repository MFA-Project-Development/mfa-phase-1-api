package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.entity.StudentClassEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface StudentClassEnrollmentRepository extends JpaRepository<StudentClassEnrollment, UUID> {

    boolean existsAllByStudentId_AndClazz(UUID studentId, Class clazz);

    @Transactional
    void deleteByStudentId_AndClazz(UUID studentId, Class clazz);

    StudentClassEnrollment findByStudentId_AndClazz(UUID studentId, Class clazz);

    Page<StudentClassEnrollment> findAllByClazz(Class clazz, Pageable pageable);

    long countStudentClassEnrollmentByClazz(Class clazz);
}
