package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.Answer;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.SubmissionRepository;
import kr.com.mfa.mfaphase1api.service.ResultService;
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
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final UserClient userClient;

    @Transactional
    @Override
    public void gradeSubmissionResult(UUID assessmentId, UUID submissionId) {

        UUID currentUserId = extractCurrentUserId();

        assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException(
                        "Assessment with ID " + assessmentId + " not found"
                ));

        Submission submission = submissionRepository
                .findBySubmissionId_AndAssessment_AssessmentId(submissionId, assessmentId)
                .orElseThrow(() -> new NotFoundException(
                        "Submission with ID " + submissionId + " not found"
                ));

        SubmissionStatus status = submission.getStatus();

        if (status == SubmissionStatus.PUBLISHED) {
            throw new ConflictException("Submission result has already been published.");
        }

        if (status == SubmissionStatus.GRADED) {
            throw new ConflictException("Submission result has already been graded.");
        }

        if (status != SubmissionStatus.SUBMITTED) {
            throw new BadRequestException(
                    "Submission result cannot be graded because it is not in submitted status."
            );
        }

        List<Question> questions = submission.getAssessment().getQuestions();
        List<Answer> answers = submission.getAnswers();

        BigDecimal maxScore = questions.stream()
                .map(Question::getPoints)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<UUID, BigDecimal> awardedByQuestionId = answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getQuestionId() != null)
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getQuestionId(),
                        a -> a.getPointsAwarded() == null ? BigDecimal.ZERO : a.getPointsAwarded(),
                        BigDecimal::add
                ));

        BigDecimal scoreEarned = questions.stream()
                .map(q -> awardedByQuestionId.getOrDefault(q.getQuestionId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (scoreEarned.compareTo(maxScore) > 0) {
            scoreEarned = maxScore;
        }

        submission.setMaxScore(maxScore);
        submission.setScoreEarned(scoreEarned);
        submission.setGradedBy(currentUserId);
        submission.setGradedAt(Instant.now());
        submission.setStatus(SubmissionStatus.GRADED);

        submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    @Override
    public SubmissionResponse getSubmissionResult(UUID assessmentId, UUID submissionId) {

        UUID currentUserId = extractCurrentUserId();
        String role = extractCurrentRole();

        boolean isInstructor = "ROLE_INSTRUCTOR".equals(role);
        boolean isStudent = "ROLE_STUDENT".equals(role);

        if (!isInstructor && !isStudent) {
            throw new ForbiddenException("Unsupported role: " + role);
        }

        Assessment assessment = isInstructor
                ? assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment with ID " + assessmentId + " not found"))
                : assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(
                        assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment with ID " + assessmentId + " not found"));

        Submission submission = isInstructor
                ? submissionRepository.findBySubmissionId_AndAssessment_AssessmentId(submissionId, assessmentId)
                .orElseThrow(() -> new NotFoundException("Submission with ID " + submissionId + " not found"))
                : submissionRepository.findBySubmissionId_AndAssessment_AndStudentId(submissionId, assessment, currentUserId)
                .orElseThrow(() -> new NotFoundException("Submission with ID " + submissionId + " not found"));

        if (submission.getStatus() != SubmissionStatus.PUBLISHED) {
            throw new NotFoundException("Submission result is not published yet.");
        }

        UserResponse user = Optional.ofNullable(userClient.getUserInfoById(submission.getStudentId()).getBody())
                .map(APIResponse::getPayload)
                .orElseThrow(() -> new NotFoundException("Student user info not found: " + submission.getStudentId()));

        StudentResponse studentResponse = StudentResponse.builder()
                .studentId(user.getUserId())
                .studentEmail(user.getEmail())
                .studentName(buildFullName(user))
                .profileImage(user.getProfileImage())
                .build();

        return submission.toResponse(studentResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<SubmissionResponse>> getAllSubmissionResults(UUID assessmentId, Integer page, Integer size, SubmissionProperty property, Sort.Direction direction) {

        UUID currentUserId = extractCurrentUserId();
        String role = extractCurrentRole();

        boolean isInstructor = "ROLE_INSTRUCTOR".equals(role);
        boolean isStudent = "ROLE_STUDENT".equals(role);

        if (!isInstructor && !isStudent) {
            throw new ForbiddenException("Unsupported role: " + role);
        }

        if (isInstructor) {
            assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Assessment with ID " + assessmentId + " not found"));
        } else {
            assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(
                            assessmentId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Assessment with ID " + assessmentId + " not found"));
        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Submission> pageSubmissions = isInstructor
                ? submissionRepository.findAllByAssessment_AssessmentIdAndAssessment_CreatedBy(assessmentId, currentUserId, pageable)
                : submissionRepository.findAllByAssessment_AssessmentIdAndStudentId(assessmentId, currentUserId, pageable);

        List<SubmissionResponse> items = pageSubmissions.stream()
                .map(submission -> {
                    if (submission.getStatus() != SubmissionStatus.PUBLISHED) {
                        throw new NotFoundException("Some submission result is not published yet.");
                    }
                    UserResponse userResponse = Optional.ofNullable(userClient.getUserInfoById(submission.getStudentId()).getBody())
                            .map(APIResponse::getPayload)
                            .orElseThrow(() -> new NotFoundException("Student user info not found: " + submission.getStudentId()));
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

    @Transactional
    @Override
    public void publishSubmissionResult(UUID assessmentId) {

        UUID currentUserId = extractCurrentUserId();

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException(
                        "Assessment with ID " + assessmentId + " not found"
                ));

        for (Submission submission : assessment.getSubmissions()) {

            if (submission.getStatus() != SubmissionStatus.GRADED) {
                throw new BadRequestException("Submission result cannot be published because some submission is not graded yet.");
            }

            submission.setStatus(SubmissionStatus.PUBLISHED);
            submission.setPublishedAt(Instant.now());
            submissionRepository.save(submission);
        }

    }


    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
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
