package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Paper;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.PaperRepository;
import kr.com.mfa.mfaphase1api.repository.SubmissionRepository;
import kr.com.mfa.mfaphase1api.service.FileService;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final PaperRepository paperRepository;
    private final FileService fileService;
    private final UserClient userClient;

    @Override
    @Transactional
    public UUID startSubmission(UUID assessmentId) {
        UUID currentUserId = extractCurrentUserId();

        Assessment assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId).orElseThrow(
                () -> new NotFoundException("Assessment not found")
        );

        Submission existSubmission = getAndValidateSubmission(assessmentId, currentUserId);

        if (existSubmission != null) {
            return existSubmission.getSubmissionId();
        }

        Submission submission = Submission.builder()
                .status(SubmissionStatus.NOT_SUBMITTED)
                .maxScore(BigDecimal.valueOf(0.00))
                .scoreEarned(BigDecimal.valueOf(0.00))
                .assessment(assessment)
                .studentId(currentUserId)
                .build();

        return submissionRepository.save(submission).getSubmissionId();
    }

    @Override
    @Transactional
    public void persistSubmissionPapers(UUID assessmentId, UUID submissionId, List<String> fileNames) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        validateFilesExist(fileNames);

        int startingPage = paperRepository.countPaperBySubmission(submission) + 1;

        List<Paper> papers = IntStream.range(0, fileNames.size())
                .mapToObj(i -> Paper.builder()
                        .page(startingPage + i)
                        .name(fileNames.get(i))
                        .submission(submission)
                        .build())
                .toList();

        paperRepository.saveAll(papers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaperResponse> getSubmissionPapers(UUID assessmentId, UUID submissionId) {
        Submission submission = submissionRepository.findBySubmissionId_AndAssessment_AssessmentId(submissionId, assessmentId).orElseThrow(
                () -> new NotFoundException("Submission " + submissionId + " found")
        );
        List<Paper> papers = paperRepository.findAllBySubmission(submission);
        return papers.stream().map(Paper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteSubmission(UUID assessmentId, UUID submissionId) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        submissionRepository.delete(submission);
    }

    @Override
    @Transactional
    public void saveSubmission(UUID assessmentId, UUID submissionId) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        submission.setStatus(SubmissionStatus.NOT_SUBMITTED);

        submissionRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<SubmissionResponse>> getAllSubmissions(UUID assessmentId, Integer page, Integer size, SubmissionProperty property, Sort.Direction direction) {

        UUID currentUserId = extractCurrentUserId();

        String currentUserRole = extractCurrentRole();

        boolean authorized = switch (currentUserRole) {
            case "ROLE_ADMIN" -> assessmentRepository.existsById(assessmentId);
            case "ROLE_INSTRUCTOR" ->
                    assessmentRepository.existsAssessmentsByAssessmentId_AndCreatedBy(assessmentId, currentUserId);
            case "ROLE_STUDENT" -> assessmentRepository
                    .existsAssessmentsByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId);
            default -> false;
        };

        if (!authorized) {
            throw new NotFoundException("Assessment " + assessmentId + " not found.");
        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Submission> pageSubmissions = switch (currentUserRole) {
            case "ROLE_ADMIN" -> submissionRepository
                    .findAll(pageable);
            case "ROLE_INSTRUCTOR" -> submissionRepository
                    .findAllByAssessment_AssessmentIdAndAssessment_CreatedBy(assessmentId, currentUserId, pageable);
            case "ROLE_STUDENT" -> submissionRepository
                    .findAllByAssessment_AssessmentIdAndStudentId(
                            assessmentId, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole);
        };

        List<SubmissionResponse> items = pageSubmissions.stream()
                .map(submission -> {
                    UserResponse userResponse = Objects.requireNonNull(userClient.getUserInfoById(submission.getStudentId()).getBody()).getPayload();
                    StudentResponse studentResponse = StudentResponse.builder()
                            .studentId(userResponse.getUserId())
                            .studentEmail(userResponse.getEmail())
                            .studentName(buildFullName(userResponse))
                            .profileImage(userResponse.getProfileImage())
                            .build();
                    return submission.toResponse(studentResponse);
                })
                .toList();

        return pageResponse(
                items,
                pageSubmissions.getTotalElements(),
                page,
                size,
                pageSubmissions.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void finalizeSubmission(UUID assessmentId, UUID submissionId) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        if (submission.getStatus() != SubmissionStatus.NOT_SUBMITTED) {
            throw new ConflictException("Submission has already been submitted for this assessment");
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private void validateFilesExist(List<String> fileNames) {
        fileNames.forEach(fileService::getFileByFileName);
    }

    private Submission getAndValidateSubmission(UUID assessmentId, UUID currentUserId) {
        Assessment assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));

        return submissionRepository.findSubmissionByAssessmentAndStudentId(assessment, currentUserId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

    private String extractCurrentRole() {
        List<String> currentUserRole = Objects.requireNonNull(JwtUtils.getJwt()).getClaimAsStringList("roles");
        return currentUserRole.getFirst();
    }

    private String buildFullName(UserResponse userResponse) {
        String firstName = userResponse.getFirstName() != null ? userResponse.getFirstName() : "";
        String lastName = userResponse.getLastName() != null ? userResponse.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }


}
