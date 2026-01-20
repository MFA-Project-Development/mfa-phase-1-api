package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentPublishRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentScheduleRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ResourceRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
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

import java.time.*;
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
    private final SubmissionRepository submissionRepository;

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

        return saved.toResponse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<AssessmentResponse>> getAllAssessmentsByClassId(UUID classId, Integer page, Integer size, AssessmentProperty property, Sort.Direction direction) {

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
                .map(
                        assessment -> {
                            Integer totalSubmitted = submissionRepository.countByAssessment(assessment);
                            return assessment.toResponse(totalSubmitted);
                        }
                )
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

        return assessment.toResponse(null);
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

        return saved.toResponse(null);

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

        if (assessment.getResources() != null) {
            for (Resource resource : assessment.getResources()) {
                fileService.deleteFileByFileName(resource.getName());
            }
        }

        for (Question question : assessment.getQuestions()) {
            if (question.getQuestionImages() != null) {
                for (QuestionImage questionImage : question.getQuestionImages()) {
                    fileService.deleteFileByFileName(questionImage.getImageUrl());
                }
            }
        }

        for (Submission submission : assessment.getSubmissions()) {
            if (submission.getPapers() != null) {
                for (Paper paper : submission.getPapers()) {
                    fileService.deleteFileByFileName(paper.getName());
                }
            }
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

        ZoneId zone;

        try {
            zone = request.getTimeZone() != null
                    ? ZoneId.of(request.getTimeZone())
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            throw new BadRequestException("Invalid time zone: " + request.getTimeZone());
        }

        Instant startDate = request.getStartDate()
                .atZone(zone)
                .toInstant();

        Instant dueDate = request.getDueDate()
                .atZone(zone)
                .toInstant();

        switch (assessment.getStatus()) {
            case AssessmentStatus.DRAFTED -> {
                assessment.setStartDate(startDate);
                assessment.setDueDate(dueDate);
                assessment.setTimeZone(request.getTimeZone());
                assessment.setClassSubSubjectInstructor(csi);
                assessment.setStatus(AssessmentStatus.SCHEDULED);

                Assessment saved = assessmentRepository.saveAndFlush(assessment);

                quartzSchedulerService.scheduleStartAndFinishJobs(saved);
            }
            case AssessmentStatus.SCHEDULED -> {
                assessment.setStartDate(dueDate);
                assessment.setDueDate(dueDate);
                assessment.setTimeZone(request.getTimeZone());

                Assessment saved = assessmentRepository.saveAndFlush(assessment);

                quartzSchedulerService.scheduleStartAndFinishJobs(saved);
            }
            default -> throw new BadRequestException("Assessment is not in drafted and scheduled status.");
        }

        return assessment.toResponse(null);
    }

    @Override
    @Transactional
    public AssessmentResponse publishAssessment(UUID classId, UUID assessmentId, AssessmentPublishRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository
                .findByAssessmentIdAndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassIdAndCreatedBy(
                        assessmentId, classId, currentUserId
                )
                .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found in class " + classId + "."));

        ClassSubSubjectInstructor csi = classSubSubjectInstructorRepository
                .findByClassSubSubject_Clazz_ClassIdAndInstructorId(classId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not assigned to any sub-subject in class " + classId + "."));

        ZoneId zone;

        try {
            zone = request.getTimeZone() != null
                    ? ZoneId.of(request.getTimeZone())
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            throw new BadRequestException("Invalid time zone: " + request.getTimeZone());
        }

        Instant startDate = Instant.now()
                .atZone(zone)
                .toInstant();

        Instant newDueDate = request.getDueDate()
                .atZone(zone)
                .toInstant();

        assessment.setStartDate(startDate);
        assessment.setDueDate(newDueDate);
        assessment.setTimeZone(request.getTimeZone());
        assessment.setClassSubSubjectInstructor(csi);
        assessment.setStatus(AssessmentStatus.STARTED);

        Assessment saved = assessmentRepository.saveAndFlush(assessment);

        quartzSchedulerService.scheduleStartAndFinishJobs(saved);

        return saved.toResponse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<AssessmentResponseForGrading>> getAllAssessments(Integer page, Integer size, AssessmentProperty property, Sort.Direction direction) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Assessment> pageAssessments = switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> assessmentRepository
                    .findAllByStatus(AssessmentStatus.FINISHED, pageable);
            case "ROLE_INSTRUCTOR" -> assessmentRepository
                    .findAllByStatusAndCreatedBy(AssessmentStatus.FINISHED, currentUserId, pageable);
            case "ROLE_STUDENT" -> assessmentRepository
                    .findAllByStatusAndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(AssessmentStatus.FINISHED, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());
        };

        List<AssessmentResponseForGrading> items = pageAssessments.stream()
                .map(assessment -> {

                    Integer totalSubmitted = submissionRepository.countByAssessment(assessment);
                    Integer totalStudents = assessment.getClassSubSubjectInstructor()
                            .getClassSubSubject()
                            .getClazz()
                            .getStudentClassEnrollments()
                            .size();

                    Integer totalPublished = submissionRepository.countByAssessmentAndPublishedAtIsNotNull(assessment);

                    boolean isPublished = totalSubmitted > 0 && totalPublished.equals(totalSubmitted);

                    Boolean isGraded = null;
                    SubmissionStatus status = null;
                    if (currentUserRole.contains("ROLE_STUDENT")) {
                        isGraded = assessment.getSubmissions()
                                .stream()
                                .anyMatch(s -> s.getGradedAt() != null);
                        status = assessment.getSubmissions()
                                .stream()
                                .map(Submission::getStatus)
                                .findFirst()
                                .orElse(null);
                    }

                    ZoneId zone = ZoneId.of(assessment.getTimeZone());

                    return AssessmentResponseForGrading.builder()
                            .assessmentId(assessment.getAssessmentId())
                            .title(assessment.getTitle())
                            .startDate(LocalDateTime.ofInstant(assessment.getStartDate(), zone))
                            .dueDate(LocalDateTime.ofInstant(assessment.getDueDate(), zone))
                            .assessmentType(assessment.getAssessmentType())
                            .subSubjectName(assessment.getClassSubSubjectInstructor().getClassSubSubject().getSubSubject().getName())
                            .className(assessment.getClassSubSubjectInstructor().getClassSubSubject().getClazz().getName())
                            .totalSubmitted(totalSubmitted)
                            .totalStudents(totalStudents)
                            .isPublished(isPublished)
                            .isGraded(isGraded)
                            .status(status)
                            .build();
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
    public AssessmentSummary getAssessmentsSummary(Month month) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        List<Assessment> assessments;

        if (month == null) {
            assessments = assessmentRepository
                    .findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId_AndStatus(currentUserId, AssessmentStatus.FINISHED);
        } else {
            int year = LocalDate.now().getYear();
            YearMonth ym = YearMonth.of(year, month);

            LocalDateTime startDate = ym.atDay(1).atStartOfDay();
            LocalDateTime dueDate = ym.plusMonths(1).atDay(1).atStartOfDay();

            ZoneId zone = ZoneId.of("UTC");

            Instant newStartDate = startDate
                    .atZone(zone)
                    .toInstant();

            Instant newDueDate = dueDate
                    .atZone(zone)
                    .toInstant();

            assessments = assessmentRepository
                    .findAllByClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId_AndStatus_AndStartDateBetween(
                            currentUserId, AssessmentStatus.FINISHED, newStartDate, newDueDate);
        }

        int exams = 0;
        int quizzes = 0;
        int assignments = 0;
        int homeworks = 0;

        for (Assessment assessment : assessments) {
            switch (assessment.getAssessmentType()) {
                case EXAM -> exams++;
                case QUIZ -> quizzes++;
                case ASSIGNMENT -> assignments++;
                case HOMEWORK -> homeworks++;
            }
        }

        return AssessmentSummary.builder()
                .exams(exams)
                .quizzes(quizzes)
                .assignments(assignments)
                .homeworks(homeworks)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AssessmentResponseForGrading> getRecentAssessments() {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> roles = JwtUtils.getJwt().getClaimAsStringList("roles");

        Pageable pageable = PageRequest.of(0, 5);

        Page<Assessment> pageAssessments = switch (roles.getFirst()) {
            case "ROLE_ADMIN" -> assessmentRepository.findRecentBySubmissionStartedAt(pageable);
            case "ROLE_INSTRUCTOR" -> assessmentRepository
                    .findRecentBySubmissionStartedAtAndInstructor(currentUserId, pageable);
            case "ROLE_STUDENT" -> assessmentRepository
                    .findRecentByMySubmissionStartedAt(currentUserId, pageable);
            default -> Page.empty(pageable);
        };

        boolean isStudent = roles.contains("ROLE_STUDENT");

        return pageAssessments.getContent().stream()
                .map(assessment -> {

                    Integer totalSubmitted = submissionRepository.countByAssessmentAndStartedAtIsNotNull(assessment);

                    Integer totalStudents = assessment.getClassSubSubjectInstructor()
                            .getClassSubSubject()
                            .getClazz()
                            .getStudentClassEnrollments()
                            .size();

                    Integer totalPublished = submissionRepository.countByAssessmentAndPublishedAtIsNotNull(assessment);

                    boolean isPublished = totalSubmitted > 0 && totalPublished.equals(totalSubmitted);

                    Boolean isGraded = null;
                    SubmissionStatus status = null;

                    if (isStudent) {
                        Submission mySubmission = submissionRepository
                                .findByAssessmentAndStudentId(assessment, currentUserId)
                                .orElse(null);

                        if (mySubmission != null) {
                            isGraded = mySubmission.getGradedAt() != null;
                            status = mySubmission.getStatus();
                        }
                    }

                    ZoneId zone = ZoneId.of(assessment.getTimeZone());

                    return AssessmentResponseForGrading.builder()
                            .assessmentId(assessment.getAssessmentId())
                            .title(assessment.getTitle())
                            .startDate(LocalDateTime.ofInstant(assessment.getStartDate(), zone))
                            .dueDate(LocalDateTime.ofInstant(assessment.getDueDate(), zone))
                            .assessmentType(assessment.getAssessmentType())
                            .subSubjectName(assessment.getClassSubSubjectInstructor()
                                    .getClassSubSubject()
                                    .getSubSubject()
                                    .getName())
                            .className(assessment.getClassSubSubjectInstructor()
                                    .getClassSubSubject()
                                    .getClazz()
                                    .getName())
                            .totalSubmitted(totalSubmitted)
                            .totalStudents(totalStudents)
                            .isPublished(isPublished)
                            .isGraded(isGraded)
                            .status(status)
                            .build();
                })
                .toList();
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
