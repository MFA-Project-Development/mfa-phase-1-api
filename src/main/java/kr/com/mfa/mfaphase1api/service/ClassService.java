package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.ClassRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MoveStudentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.ClassProperty;
import kr.com.mfa.mfaphase1api.model.enums.ClassSubSubjectProperty;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ClassService {
    ClassResponse createClass(ClassRequest request);

    PagedResponse<List<ClassResponse>> getAllClasses(Integer page, Integer size, ClassProperty property, Sort.Direction direction);

    ClassResponse getClassById(UUID classId);

    ClassResponse updateClassById(UUID classId, ClassRequest request);

    void deleteClassById(UUID classId);

    void assignSubSubjectToClass(UUID classId, UUID subSubjectId);

    void unassignSubSubjectFromClass(UUID classId, UUID subSubjectId);

    void assignInstructorToClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId, LocalDate startDate);

    void unassignInstructorFromClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId);

    void leaveInstructorFromClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId, LocalDate endDate);

    void enrollStudentToClass(UUID classId, UUID studentId, LocalDate startDate);

    void unenrollStudentFromClass(UUID classId, UUID studentId);

    void leaveStudentFromClass(UUID classId, UUID studentId, LocalDate endDate);

    void moveStudentToAnotherClass(MoveStudentRequest request);

    PagedResponse<List<SubSubjectResponse>> getSubSubjectsOfClass(UUID classId, Integer page, Integer size, ClassSubSubjectProperty property, Sort.Direction direction);

    PagedResponse<List<StudentResponse>> getStudentsByClass(UUID classId, Integer page, Integer size, Sort.Direction direction);

    PagedResponse<List<InstructorResponse>> getInstructorsByClass(UUID classId, Integer page, Integer size, Sort.Direction direction);

    ClassSummaryResponse getClassSummary(UUID classId);

    PagedResponse<List<ClassResponse>> getClassesOfStudent(UUID studentId, Integer page, Integer size, ClassProperty property, Sort.Direction direction);

    PagedResponse<List<ClassResponse>> getClassesOfInstructor(UUID instructorId, Integer page, Integer size, ClassProperty property, Sort.Direction direction);

    void assignMultipleSubSubjectToClass(UUID classId, List<UUID> subSubjectIds);

    void multipleEnrollStudentToClass(UUID classId, List<UUID> studentIds);
}
