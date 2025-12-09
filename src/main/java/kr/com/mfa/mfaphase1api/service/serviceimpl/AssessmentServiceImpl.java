package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentScheduleRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ResourceRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.ResourceResponse;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.AssessmentService;
import kr.com.mfa.mfaphase1api.service.FileService;
import kr.com.mfa.mfaphase1api.service.QuartzSchedulerService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    //    private final AssessmentTypeRepository assessmentTypeRepository;
    private final ClassRepository classRepository;
    private final ClassSubSubjectInstructorRepository classSubSubjectInstructorRepository;
    private final ResourceRepository resourceRepository;
    private final FileService fileService;
    private final QuartzSchedulerService quartzSchedulerService;

    @Override
    @Transactional
    public AssessmentResponse createAssessment(UUID classId, AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        ClassSubSubjectInstructor csi =
                classSubSubjectInstructorRepository
                        .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                        .orElseThrow(() -> new ForbiddenException(
                                "You are not assigned to any sub-subject in class " + classId + "."
                        ));

        Assessment saved = assessmentRepository.saveAndFlush(
                request.toEntity(currentUserId, csi)
        );

        return saved.toResponse();
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

        List<AssessmentResponse> items = pageAssessments.stream()
                .map(Assessment::toResponse)
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

        return assessment.toResponse();
    }

    @Override
    @Transactional
    public AssessmentResponse updateAssessmentById(UUID classId, UUID assessmentId, AssessmentRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found in class " + classId + "."));

//        AssessmentType assessmentType = assessmentTypeRepository.findById(request.getAssessmentTypeId())
//                .orElseThrow(() -> new NotFoundException("Assessment type " + request.getAssessmentTypeId() + " not found."));

        ClassSubSubjectInstructor csi = classSubSubjectInstructorRepository
                .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not assigned to any sub-subject in class " + classId + "."));

        assessment.setAssessmentType(request.getAssessmentType());
        assessment.setClassSubSubjectInstructor(csi);
        assessment.setTitle(request.getTitle().trim());
        assessment.setDescription(request.getDescription());
        assessment.setTimeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : assessment.getTimeLimit());

        Assessment saved = assessmentRepository.saveAndFlush(assessment);

        return saved.toResponse();

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

    @Override
    @Transactional
    public void persistAssessmentResource(UUID classId, UUID assessmentId, ResourceKind kind, List<ResourceRequest> requests) {
        UUID currentUserId = extractCurrentUserId();

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Assessment " + assessmentId + " not found in class " + classId + "."
                ));

        List<String> fileNames = requests.stream()
                .map(ResourceRequest::getName)
                .filter(Objects::nonNull)
                .toList();

        validateFilesExist(fileNames);

        List<Resource> resources = requests.stream()
                .filter(request -> request.getName() != null)
                .map(request -> Resource.builder()
                        .kind(kind)
                        .title(request.getTitle())
                        .name(request.getName())
                        .assessment(assessment)
                        .build())
                .toList();

        resourceRepository.saveAll(resources);
    }

    @Override
    public List<ResourceResponse> getAssessmentResources(UUID classId, UUID assessmentId) {
        UUID currentUserId = extractCurrentUserId();

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Assessment " + assessmentId + " not found in class " + classId + "."
                ));

        List<Resource> resources = resourceRepository.findAllByAssessment(assessment);

        return resources.stream().map(Resource::toResponse).toList();
    }

    @Override
    @Transactional
    public AssessmentResponse scheduleAssessment(UUID classId, UUID assessmentId, AssessmentScheduleRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found in class " + classId + "."));

        ClassSubSubjectInstructor csi = classSubSubjectInstructorRepository
                .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not assigned to any sub-subject in class " + classId + "."));

        if (request.getStartDate().isAfter(request.getDueDate())) {
            throw new BadRequestException("startAt must be before endAt.");
        }

        switch (assessment.getStatus()) {
            case AssessmentStatus.DRAFTED -> {
                assessment.setStartDate(request.getStartDate());
                assessment.setDueDate(request.getDueDate());
                assessment.setClassSubSubjectInstructor(csi);
                assessment.setStatus(AssessmentStatus.SCHEDULED);

                Assessment saved = assessmentRepository.saveAndFlush(assessment);

                quartzSchedulerService.scheduleStartAndFinishJobs(saved);
            }
            case AssessmentStatus.SCHEDULED -> {
                assessment.setStartDate(request.getStartDate());
                assessment.setDueDate(request.getDueDate());

                Assessment saved = assessmentRepository.saveAndFlush(assessment);

                quartzSchedulerService.scheduleStartAndFinishJobs(saved);
            }
            default -> throw new BadRequestException("Assessment is not in drafted and scheduled status.");
        }

        return assessment.toResponse();
    }

    @Override
    @Transactional
    public AssessmentResponse publishAssessment(UUID classId, UUID assessmentId, LocalDateTime dueDate) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found in class " + classId + "."));

        ClassSubSubjectInstructor csi = classSubSubjectInstructorRepository
                .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not assigned to any sub-subject in class " + classId + "."));

        assessment.setStartDate(LocalDateTime.now());
        assessment.setDueDate(dueDate);
        assessment.setClassSubSubjectInstructor(csi);
        assessment.setStatus(AssessmentStatus.STARTED);

        Assessment saved = assessmentRepository.saveAndFlush(assessment);

        quartzSchedulerService.scheduleStartAndFinishJobs(saved);

        return saved.toResponse();
    }

    private Assessment getOrThrow(UUID classId, UUID assessmentId) {
        return assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(
                        assessmentId, classId)
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found."));
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private void validateFilesExist(List<String> fileNames) {
        fileNames.forEach(fileService::getFileByFileName);
    }
}
