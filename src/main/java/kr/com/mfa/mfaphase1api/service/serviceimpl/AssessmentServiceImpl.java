package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.ClassResponse;
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
    private final ClassRepository classRepository;
    private final ClassSubSubjectInstructorRepository classSubSubjectInstructorRepository;

    private final UserClient userClient;

    @Override
    @Transactional
    public AssessmentResponse createAssessment(UUID classId, AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        UserResponse userResponse = getUserOrThrow(currentUserId);

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
                .orElseThrow(() -> new NotFoundException(
                        "Assessment type " + request.getAssessmentTypeId() + " not found."
                ));

        ClassSubSubjectInstructor csi =
                classSubSubjectInstructorRepository
                        .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                        .orElseThrow(() -> new ForbiddenException(
                                "You are not assigned to any sub-subject in class " + classId + "."
                        ));

        Assessment saved = assessmentRepository.saveAndFlush(
                request.toEntity(currentUserId, assessmentType, csi)
        );

        Class clazz = csi.getClassSubSubject().getClazz();
        SubSubject subSubject = csi.getClassSubSubject().getSubSubject();

        return saved.toResponse(
                userResponse,
                assessmentType.toResponse(),
                subSubject.toResponse(),
                clazz.toResponse()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<AssessmentResponse>> getAllAssessments(UUID classId, Integer page, Integer size, AssessmentProperty property, Sort.Direction direction) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        boolean authorized = switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> classRepository.existsById(classId);
            case "ROLE_INSTRUCTOR" -> classRepository
                    .existsByClassIdAndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(classId, currentUserId);
            case "ROLE_STUDENT" -> classRepository
                    .existsByClassIdAndStudentClassEnrollments_StudentId(classId, currentUserId);
            default -> false;
        };

        if (!authorized) {
            throw new NotFoundException("Class " + classId + " not found.");
        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Assessment> pageAssessments = switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> assessmentRepository
                    .findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(classId, pageable);
            case "ROLE_INSTRUCTOR" -> assessmentRepository
                    .findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndCreatedBy(classId, currentUserId, pageable);
            case "ROLE_STUDENT" -> assessmentRepository
                    .findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(
                            classId, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        };

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class " + classId + " not found."));
        ClassResponse clazzResp = clazz.toResponse();

        Map<UUID, UserResponse> creatorCache = new HashMap<>();

        List<AssessmentResponse> items = pageAssessments.stream()
                .map(assessment -> {
                    UUID creatorId = assessment.getCreatedBy();
                    UserResponse creator = creatorCache.computeIfAbsent(creatorId, this::getUserOrThrow);

                    var assessmentTypeResp = assessment.getAssessmentType().toResponse();
                    var subSubjectResp = assessment.getClassSubSubjectInstructor()
                            .getClassSubSubject()
                            .getSubSubject()
                            .toResponse();

                    return assessment.toResponse(creator, assessmentTypeResp, subSubjectResp, clazzResp);
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
    public AssessmentResponse getAssessmentById(UUID classId, UUID assessmentId) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Assessment assessment = switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> getOrThrow(classId, assessmentId);
            case "ROLE_INSTRUCTOR" -> assessmentRepository
                    .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                            assessmentId, classId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found."));
            case "ROLE_STUDENT" -> assessmentRepository
                    .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(
                            assessmentId, classId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found."));
            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        };

        AssessmentType assessmentType = assessment.getAssessmentType();
        Class clazz = assessment.getClassSubSubjectInstructor().getClassSubSubject().getClazz();
        SubSubject subSubject = assessment.getClassSubSubjectInstructor().getClassSubSubject().getSubSubject();
        UserResponse creator = getUserOrThrow(assessment.getCreatedBy());

        return assessment.toResponse(creator, assessmentType.toResponse(), subSubject.toResponse(), clazz.toResponse());
    }

    @Override
    @Transactional
    public AssessmentResponse updateAssessmentById(UUID classId, UUID assessmentId, AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        UserResponse creator = getUserOrThrow(currentUserId);

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found in class " + classId + "."));

        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
                .orElseThrow(() -> new NotFoundException("Assessment type " + request.getAssessmentTypeId() + " not found."));

        ClassSubSubjectInstructor csi = classSubSubjectInstructorRepository
                .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not assigned to any sub-subject in class " + classId + "."));

        assessment.setAssessmentType(assessmentType);
        assessment.setClassSubSubjectInstructor(csi);
        assessment.setTitle(request.getTitle().trim());
        assessment.setDescription(request.getDescription());
        assessment.setStartDate(request.getStartDate());
        assessment.setDueDate(request.getDueDate());
        assessment.setTimeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : assessment.getTimeLimit());
        assessment.setStatus(request.getStatus());

        Assessment saved = assessmentRepository.saveAndFlush(assessment);

        Class clazz = csi.getClassSubSubject().getClazz();
        SubSubject subSubject = csi.getClassSubSubject().getSubSubject();

        return saved.toResponse(
                creator,
                assessmentType.toResponse(),
                subSubject.toResponse(),
                clazz.toResponse()
        );

    }

    @Override
    @Transactional
    public void deleteAssessmentById(UUID classId, UUID assessmentId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Assessment " + assessmentId + " not found in class " + classId + "."
                ));

        boolean hasAssignment = classSubSubjectInstructorRepository
                .existsByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId);
        if (!hasAssignment) {
            throw new ForbiddenException("You are not assigned to this class.");
        }

        assessmentRepository.delete(assessment);

    }


    private Assessment getOrThrow(UUID classId, UUID assessmentId) {
        return assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(
                        assessmentId, classId)
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found."));
    }

    private UserResponse getUserOrThrow(UUID userId) {
        var response = userClient.getUserInfoById(userId);
        if (response == null || response.getBody() == null || response.getBody().getPayload() == null) {
            throw new NotFoundException("User " + userId + " not found");
        }
        return response.getBody().getPayload();
    }

}
