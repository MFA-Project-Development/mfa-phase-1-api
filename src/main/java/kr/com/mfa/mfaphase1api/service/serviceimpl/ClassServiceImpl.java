package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.ClassRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MoveStudentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.UserIdsRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.ClassProperty;
import kr.com.mfa.mfaphase1api.model.enums.ClassSubSubjectProperty;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.ClassService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {

    private static final String PREFIX = "CLS-";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClassRepository classRepository;
    private final ClassSubSubjectRepository classSubSubjectRepository;
    private final SubSubjectRepository subSubjectRepository;
    private final ClassSubSubjectInstructorRepository classSubSubjectInstructorRepository;
    private final StudentClassEnrollmentRepository studentClassEnrollmentRepository;
    private final AssessmentRepository assessmentRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        assertClassNameUnique(request.getName());
        String code = generateClassCode();
        request.setCode(code);
        Class saved = classRepository.save(request.toEntity());
        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<ClassResponse>> getAllClasses(
            Integer page,
            Integer size,
            ClassProperty property,
            Sort.Direction direction
    ) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Class> pageClasses;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> pageClasses = classRepository.findAll(pageable);

            case "ROLE_INSTRUCTOR" ->
                    pageClasses = classRepository.findAllByClassSubSubjects_ClassSubSubjectInstructors_InstructorId(currentUserId, pageable);

            case "ROLE_STUDENT" ->
                    pageClasses = classRepository.findAllByStudentClassEnrollments_StudentId(currentUserId, pageable);

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        }

        List<ClassResponse> items = pageClasses
                .getContent()
                .stream()
                .map(Class::toResponse)
                .toList();

        return pageResponse(
                items,
                pageClasses.getTotalElements(),
                page,
                size,
                pageClasses.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClassById(UUID classId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Class clazz;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> clazz = getOrThrow(classId);

            case "ROLE_INSTRUCTOR" ->
                    clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(classId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Class not found"));

            case "ROLE_STUDENT" ->
                    clazz = classRepository.findByClassId_AndStudentClassEnrollments_StudentId(classId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Class not found"));

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        }

        return clazz.toResponse();
    }

    @Override
    @Transactional
    public ClassResponse updateClassById(UUID classId, ClassRequest request) {
        getOrThrow(classId);
        assertClassNameUnique(request.getName());
        Class saved = classRepository.save(request.toEntity(classId));
        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteClassById(UUID classId) {
        getOrThrow(classId);
        classRepository.deleteById(classId);
    }

    @Override
    @Transactional
    public void assignSubSubjectToClass(UUID classId, UUID subSubjectId) {

        boolean existsSubSubjectAndClass = classSubSubjectRepository.existsClassSubSubjectByClazz_ClassId_AndSubSubject_SubSubjectId(classId, subSubjectId);

        if (existsSubSubjectAndClass) {
            throw new ConflictException("SubSubject already assigned to this class");
        }

        Class clazz = getOrThrow(classId);

        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not found"));

        ClassSubSubject classSubSubject = ClassSubSubject.builder()
                .clazz(clazz)
                .subSubject(subSubject)
                .build();

        classSubSubjectRepository.save(classSubSubject);
    }

    @Override
    @Transactional
    public void unassignSubSubjectFromClass(UUID classId, UUID subSubjectId) {

        boolean existsSubSubjectAndClass = classSubSubjectRepository.existsClassSubSubjectByClazz_ClassId_AndSubSubject_SubSubjectId(classId, subSubjectId);

        if (!existsSubSubjectAndClass) {
            throw new BadRequestException("SubSubject already unassigned from this class");
        }

        Class clazz = getOrThrow(classId);

        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not found"));

        classSubSubjectRepository.deleteClassSubSubjectByClazz_AndSubSubject(clazz, subSubject);

    }

    @Override
    @Transactional
    public void assignInstructorToClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId, LocalDate startDate) {

        Class clazz = getOrThrow(classId);

        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not found"));

        ClassSubSubject classSubSubject = classSubSubjectRepository.findClassSubSubjectByClazz_AndSubSubject(clazz, subSubject)
                .orElseThrow(() -> new NotFoundException("ClassSubSubject not found"));

        UserResponse userResponse = getUserOrThrow(instructorId);

        boolean existsInstructorAndClassSubSubject = classSubSubjectInstructorRepository.existsClassSubSubjectInstructorByClassSubSubject_ClassSubSubjectId_AndInstructorId(classSubSubject.getClassSubSubjectId(), instructorId);

        if (existsInstructorAndClassSubSubject) {
            throw new ConflictException("Instructor already assigned to this class sub-subject");
        }

        ClassSubSubjectInstructor classSubSubjectInstructor = ClassSubSubjectInstructor.builder()
                .classSubSubject(classSubSubject)
                .instructorId(userResponse.getUserId())
                .startDate(startDate != null ? startDate : LocalDate.now())
                .build();

        classSubSubjectInstructorRepository.save(classSubSubjectInstructor);
    }

    @Override
    @Transactional
    public void unassignInstructorFromClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId) {

        Class clazz = getOrThrow(classId);

        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not found"));

        ClassSubSubject classSubSubject = classSubSubjectRepository.findClassSubSubjectByClazz_AndSubSubject(clazz, subSubject)
                .orElseThrow(() -> new NotFoundException("ClassSubSubject not found"));

        UserResponse userResponse = getUserOrThrow(instructorId);

        ClassSubSubjectInstructor classSubSubjectInstructor = classSubSubjectInstructorRepository.findClassSubSubjectInstructorByClassSubSubject_AndInstructorId(classSubSubject, userResponse.getUserId()).orElseThrow(
                () -> new NotFoundException("Instructor already unassigned to this class sub-subject")
        );

        classSubSubjectInstructorRepository.deleteClassSubSubjectInstructorByClassSubSubject_AndInstructorId(classSubSubjectInstructor.getClassSubSubject(), userResponse.getUserId());
    }

    @Override
    @Transactional
    public void leaveInstructorFromClassSubSubject(UUID classId, UUID subSubjectId, UUID instructorId, LocalDate endDate) {

        Class clazz = getOrThrow(classId);

        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not found"));

        ClassSubSubject classSubSubject = classSubSubjectRepository.findClassSubSubjectByClazz_AndSubSubject(clazz, subSubject)
                .orElseThrow(() -> new NotFoundException("ClassSubSubject not found"));

        UserResponse userResponse = getUserOrThrow(instructorId);

        ClassSubSubjectInstructor classSubSubjectInstructor = classSubSubjectInstructorRepository.findClassSubSubjectInstructorByClassSubSubject_AndInstructorId(classSubSubject, userResponse.getUserId()).orElseThrow(
                () -> new NotFoundException("Instructor already leave to this class sub-subject")
        );

        classSubSubjectInstructor.setEndDate(endDate != null ? endDate : LocalDate.now());

        classSubSubjectInstructorRepository.save(classSubSubjectInstructor);

    }

    @Override
    @Transactional
    public void enrollStudentToClass(UUID classId, UUID studentId, LocalDate startDate) {
        Class clazz = getOrThrow(classId);

        UserResponse userResponse = getUserOrThrow(studentId);

        boolean existsStudentAndClass = studentClassEnrollmentRepository.existsAllByStudentId_AndClazz(studentId, clazz);

        if (existsStudentAndClass) {
            throw new ConflictException("Student already enrolled to this class");
        }

        StudentClassEnrollment studentClassEnrollment = StudentClassEnrollment.builder()
                .clazz(clazz)
                .studentId(userResponse.getUserId())
                .startDate(startDate != null ? startDate : LocalDate.now())
                .build();

        studentClassEnrollmentRepository.save(studentClassEnrollment);
    }

    @Override
    @Transactional
    public void unenrollStudentFromClass(UUID classId, UUID studentId) {
        Class clazz = getOrThrow(classId);

        UserResponse userResponse = getUserOrThrow(studentId);

        StudentClassEnrollment studentClassEnrollment = studentClassEnrollmentRepository.findByStudentId_AndClazz(userResponse.getUserId(), clazz);

        if (studentClassEnrollment == null) {
            throw new NotFoundException("Student already unenrolled to this class");
        }

        studentClassEnrollmentRepository.deleteByStudentId_AndClazz(userResponse.getUserId(), clazz);
    }

    @Override
    @Transactional
    public void leaveStudentFromClass(UUID classId, UUID studentId, LocalDate endDate) {
        Class clazz = getOrThrow(classId);

        UserResponse userResponse = getUserOrThrow(studentId);

        StudentClassEnrollment studentClassEnrollment = studentClassEnrollmentRepository.findByStudentId_AndClazz(userResponse.getUserId(), clazz);

        if (studentClassEnrollment == null) {
            throw new NotFoundException("Student already leave to this class");
        }

        studentClassEnrollment.setEndDate(endDate != null ? endDate : LocalDate.now());

        studentClassEnrollmentRepository.save(studentClassEnrollment);
    }

    @Override
    @Transactional
    public void moveStudentToAnotherClass(MoveStudentRequest request) {
        UUID fromClassId = request.getFromClassId();
        UUID toClassId = request.getToClassId();
        UUID studentId = request.getStudentId();
        LocalDate moveDate = request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDate.now();
        String moveReason = request.getMoveReason();

        if (request.getFromClassId().equals(request.getToClassId())) {
            throw new ConflictException("Source and destination class cannot be the same");
        }

        Class fromClazz = classRepository.findById(fromClassId)
                .orElseThrow(() -> new NotFoundException("Source class not found"));

        Class toClazz = classRepository.findById(toClassId)
                .orElseThrow(() -> new NotFoundException("Destination class not found"));

        UserResponse userResponse = getUserOrThrow(studentId);

        StudentClassEnrollment currentStudentClassEnrollment = studentClassEnrollmentRepository.findByStudentId_AndClazz(userResponse.getUserId(), fromClazz);

        if (currentStudentClassEnrollment == null) {
            throw new NotFoundException("Active enrollment not found for student in the source class");
        }

        currentStudentClassEnrollment.setEndDate(moveDate);
        currentStudentClassEnrollment.setMoveReason("Moved to class: " + toClazz.getName() + " Reason: " + moveReason);
        studentClassEnrollmentRepository.save(currentStudentClassEnrollment);

        boolean existsStudentAndClass = studentClassEnrollmentRepository.existsAllByStudentId_AndClazz(studentId, toClazz);

        if (existsStudentAndClass) {
            throw new ConflictException("Student already moved to this class");
        }

        StudentClassEnrollment newStudentClassEnrollment = StudentClassEnrollment.builder()
                .clazz(toClazz)
                .studentId(userResponse.getUserId())
                .startDate(moveDate)
                .build();

        studentClassEnrollmentRepository.save(newStudentClassEnrollment);

    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<SubSubjectResponse>> getSubSubjectsOfClass(UUID classId, Integer page, Integer size, ClassSubSubjectProperty property, Sort.Direction direction) {

        Class clazz = getOrThrow(classId);

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<ClassSubSubject> pageClassSubSubjects = classSubSubjectRepository.findAllByClazz(clazz, pageable);

        List<SubSubjectResponse> items = pageClassSubSubjects.getContent().stream()
                .map(css -> css.getSubSubject().toResponse())
                .toList();

        return pageResponse(
                items,
                pageClassSubSubjects.getTotalElements(),
                page,
                size,
                pageClassSubSubjects.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<UserResponse>> getStudentsByClass(UUID classId, Integer page, Integer size, Sort.Direction direction) {
        Class clazz = getOrThrow(classId);

        int safePage = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(safePage, size, Sort.by(direction, "startDate"));

        Page<StudentClassEnrollment> pageEnrollments =
                studentClassEnrollmentRepository.findAllByClazz(clazz, pageable);

        List<UUID> studentIds = pageEnrollments.getContent().stream()
                .map(StudentClassEnrollment::getStudentId)
                .toList();

        if (studentIds.isEmpty()) {
            return pageResponse(List.of(), 0L, page, size, 0);
        }

        UserIdsRequest request = UserIdsRequest.builder().userIds(studentIds).build();
        var response = userClient.getAllUserByUserIds(request);

        List<UserResponse> students = (response != null && response.getBody() != null)
                ? response.getBody().getPayload()
                : List.of();

        return pageResponse(
                students,
                pageEnrollments.getTotalElements(),
                page,
                size,
                pageEnrollments.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<UserResponse>> getInstructorsByClass(UUID classId, Integer page, Integer size, Sort.Direction direction) {
        Class clazz = getOrThrow(classId);

        int safePage = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(safePage, size, Sort.by(direction, "startDate"));

        Page<ClassSubSubjectInstructor> pageInstructors =
                classSubSubjectInstructorRepository.findDistinctByClassSubSubject_Clazz(clazz, pageable);

        List<UUID> instructorIds = pageInstructors.getContent().stream()
                .map(ClassSubSubjectInstructor::getInstructorId)
                .distinct()
                .toList();

        if (instructorIds.isEmpty()) {
            return pageResponse(List.of(), 0L, page, size, 0);
        }

        UserIdsRequest request = UserIdsRequest.builder().userIds(instructorIds).build();
        var response = userClient.getAllUserByUserIds(request);

        List<UserResponse> instructors = (response != null && response.getBody() != null)
                ? response.getBody().getPayload()
                : List.of();

        return pageResponse(
                instructors,
                pageInstructors.getTotalElements(),
                page,
                size,
                pageInstructors.getTotalPages()
        );
    }

    @Override
    public ClassSummaryResponse getClassSummary(UUID classId) {
        Class clazz = getOrThrow(classId);

        long subSubjects = classSubSubjectRepository.countByClazz(clazz);
        long totalStudents = studentClassEnrollmentRepository.countStudentClassEnrollmentByClazz(clazz);
        long totalInstructors = classSubSubjectInstructorRepository.countDistinctByClassSubSubject_Clazz(clazz);

        return new ClassSummaryResponse(
                clazz.getClassId(),
                clazz.getName(),
                subSubjects,
                totalStudents,
                totalInstructors
        );
    }

    @Override
    public PagedResponse<List<ClassResponse>> getClassesOfStudent(UUID studentId, Integer page, Integer size, ClassProperty property, Sort.Direction direction) {

        UserResponse userResponse = getUserOrThrow(studentId);

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Class> pageClasses = classRepository.findAllByStudentClassEnrollments_StudentId(userResponse.getUserId(), pageable);

        List<ClassResponse> items = pageClasses.getContent().stream()
                .map(Class::toResponse)
                .toList();

        return pageResponse(
                items,
                pageClasses.getTotalElements(),
                page,
                size,
                pageClasses.getTotalPages()
        );
    }

    @Override
    public PagedResponse<List<ClassResponse>> getClassesOfInstructor(UUID instructorId, Integer page, Integer size, ClassProperty property, Sort.Direction direction) {
        UserResponse userResponse = getUserOrThrow(instructorId);

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Class> pageClasses = classRepository.findAllByClassSubSubjects_ClassSubSubjectInstructors_InstructorId(userResponse.getUserId(), pageable);

        List<ClassResponse> items = pageClasses.getContent().stream()
                .map(Class::toResponse)
                .toList();

        return pageResponse(
                items,
                pageClasses.getTotalElements(),
                page,
                size,
                pageClasses.getTotalPages()
        );
    }

    @Override
    public PagedResponse<List<AssessmentResponse>> getAssessmentsByClassId(UUID classId, Integer page, Integer size, AssessmentProperty property, Sort.Direction direction) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Class clazz;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> clazz = getOrThrow(classId);

            case "ROLE_INSTRUCTOR" ->
                    clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(classId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Class not found"));

            case "ROLE_STUDENT" ->
                    clazz = classRepository.findByClassId_AndStudentClassEnrollments_StudentId(classId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Class not found"));

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Assessment> pageAssessments;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" ->
                    pageAssessments = assessmentRepository.findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(clazz.getClassId(), pageable);

            case "ROLE_INSTRUCTOR" ->
                    pageAssessments = assessmentRepository.findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndCreatedBy(clazz.getClassId(), currentUserId, pageable);

            case "ROLE_STUDENT" ->
                    pageAssessments = assessmentRepository.findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(clazz.getClassId(), currentUserId, pageable);

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        List<AssessmentResponse> items = pageAssessments
                .getContent()
                .stream()
                .map(assessment -> {

                    AssessmentType assessmentType = assessment.getAssessmentType();

                    SubSubject subSubject = assessment.getClassSubSubjectInstructor().getClassSubSubject().getSubSubject();

                    UserResponse userResponse = getUserOrThrow(assessment.getCreatedBy());

                    return assessment.toResponse(userResponse, assessmentType.toResponse(), subSubject.toResponse(), clazz.toResponse());

                })
                .toList();

        return pageResponse(
                items,
                pageAssessments.getTotalElements(),
                page,
                size,
                pageAssessments.getTotalPages()
        );

    }


    private Class getOrThrow(UUID classId) {
        return classRepository.findById(classId)
                .orElseThrow(() ->
                        new NotFoundException("Class with ID " + classId + " not found"));
    }

    private void assertClassNameUnique(String name) {
        if (classRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Class with name '" + name + "' already exists");
        }
    }

    private String generateClassCode() {
        StringBuilder code = new StringBuilder(PREFIX);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            code.append(ALPHANUMERIC.charAt(index));
        }
        return code.toString();
    }

    private UserResponse getUserOrThrow(UUID userId) {
        var response = userClient.getUserInfoById(userId);
        if (response == null || response.getBody() == null || response.getBody().getPayload() == null) {
            throw new NotFoundException("User with ID %s not found".formatted(userId));
        }
        return response.getBody().getPayload();
    }


}
