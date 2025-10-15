package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.UserResponse;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.AssessmentService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentTypeRepository assessmentTypeRepository;
    private final SubSubjectRepository subSubjectRepository;
    private final ClassRepository classRepository;
    private final ClassSubSubjectRepository classSubSubjectRepository;
    private final ClassSubSubjectInstructorRepository classSubSubjectInstructorRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public AssessmentResponse createAssessment(AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        UserResponse userResponse = getUserOrThrow(currentUserId);

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId()).orElseThrow(
                () -> new NotFoundException("AssessmentType not found")
        );

        Class clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(request.getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("Class not found")
        );

        SubSubject subSubject = subSubjectRepository.findByClassSubSubjects_Clazz_ClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(request.getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("SubSubject not found")
        );

        ClassSubSubject classSubSubject = classSubSubjectRepository.findClassSubSubjectByClazz_AndSubSubject(clazz, subSubject).orElseThrow(
                () -> new NotFoundException("ClassSubSubject not found")
        );

        ClassSubSubjectInstructor classSubSubjectInstructor = classSubSubjectInstructorRepository.findClassSubSubjectInstructorByClassSubSubject_AndInstructorId(classSubSubject, currentUserId).orElseThrow(
                () -> new NotFoundException("ClassSubSubjectInstructor not found")
        );

        Assessment saved = assessmentRepository.saveAndFlush(request.toEntity(currentUserId, assessmentType, classSubSubjectInstructor));

        return saved.toResponse(userResponse, assessmentType.toResponse(), subSubject.toResponse(), clazz.toResponse());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<AssessmentResponse>> getAllAssessments(Integer page, Integer size, AssessmentProperty property, Sort.Direction direction) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Assessment> pageAssessments;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> pageAssessments = assessmentRepository.findAll(pageable);

            case "ROLE_INSTRUCTOR" ->
                    pageAssessments = assessmentRepository.findAllByCreatedBy(currentUserId, pageable);

//            case "ROLE_STUDENT" ->
//                    pageAssessments = assessmentRepository.findAllByStudentClassEnrollments_StudentId(currentUserId, pageable);

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        List<AssessmentResponse> items = pageAssessments
                .getContent()
                .stream()
                .map(assessment -> {

                    AssessmentType assessmentType = assessmentTypeRepository.findById(assessment.getAssessmentType().getAssessmentTypeId()).orElseThrow(
                            () -> new NotFoundException("AssessmentType not found")
                    );

                    Class clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(assessment.getClassSubSubjectInstructor().getClassSubSubject().getClazz().getClassId(), currentUserId).orElseThrow(
                            () -> new NotFoundException("Class not found")
                    );

                    SubSubject subSubject = subSubjectRepository.findByClassSubSubjects_Clazz_ClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(clazz.getClassId(), currentUserId).orElseThrow(
                            () -> new NotFoundException("SubSubject not found")
                    );

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

    @Override
    @Transactional(readOnly = true)
    public AssessmentResponse getAssessmentById(UUID assessmentId) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Assessment assessment;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> assessment = getOrThrow(assessmentId);

            case "ROLE_INSTRUCTOR" ->
                    assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Assessment not found"));

//            case "ROLE_STUDENT" ->
//                    pageAssessments = assessmentRepository.findAllByStudentClassEnrollments_StudentId(currentUserId, pageable);

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        AssessmentType assessmentType = assessmentTypeRepository.findById(assessment.getAssessmentType().getAssessmentTypeId()).orElseThrow(
                () -> new NotFoundException("AssessmentType not found")
        );

        Class clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(assessment.getClassSubSubjectInstructor().getClassSubSubject().getClazz().getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("Class not found")
        );

        SubSubject subSubject = subSubjectRepository.findByClassSubSubjects_Clazz_ClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(clazz.getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("SubSubject not found")
        );

        UserResponse userResponse = getUserOrThrow(currentUserId);

        return assessment.toResponse(userResponse, assessmentType.toResponse(), subSubject.toResponse(), clazz.toResponse());
    }

    @Override
    @Transactional
    public AssessmentResponse updateAssessmentById(UUID assessmentId, AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        UserResponse userResponse = getUserOrThrow(currentUserId);

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId()).orElseThrow(
                () -> new NotFoundException("AssessmentType not found")
        );

        Class clazz = classRepository.findByClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(request.getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("Class not found")
        );

        SubSubject subSubject = subSubjectRepository.findByClassSubSubjects_Clazz_ClassId_AndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(request.getClassId(), currentUserId).orElseThrow(
                () -> new NotFoundException("SubSubject not found")
        );

        ClassSubSubject classSubSubject = classSubSubjectRepository.findClassSubSubjectByClazz_AndSubSubject(clazz, subSubject).orElseThrow(
                () -> new NotFoundException("ClassSubSubject not found")
        );

        ClassSubSubjectInstructor classSubSubjectInstructor = classSubSubjectInstructorRepository.findClassSubSubjectInstructorByClassSubSubject_AndInstructorId(classSubSubject, currentUserId).orElseThrow(
                () -> new NotFoundException("ClassSubSubjectInstructor not found")
        );

        Assessment saved = assessmentRepository.saveAndFlush(request.toEntity(assessment.getAssessmentId(), currentUserId, assessmentType, classSubSubjectInstructor));

        return saved.toResponse(userResponse, assessmentType.toResponse(), subSubject.toResponse(), clazz.toResponse());

    }

    @Override
    @Transactional
    public void deleteAssessmentById(UUID assessmentId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));

        assessmentRepository.deleteByAssessmentId_AndCreatedBy(assessment.getAssessmentId(), currentUserId);

    }


    private Assessment getOrThrow(UUID assessmentId) {
        return assessmentRepository.findById(assessmentId)
                .orElseThrow(() ->
                        new NotFoundException("Assessment not found"));
    }

    private UserResponse getUserOrThrow(UUID userId) {
        var response = userClient.getUserInfoById(userId);
        if (response == null || response.getBody() == null || response.getBody().getPayload() == null) {
            throw new NotFoundException("User not found");
        }
        return response.getBody().getPayload();
    }

}
