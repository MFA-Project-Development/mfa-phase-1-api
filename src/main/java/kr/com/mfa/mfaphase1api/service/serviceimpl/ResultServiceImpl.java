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
import kr.com.mfa.mfaphase1api.model.enums.SubmissionSort;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.model.enums.TimeRange;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.FeedbackRepository;
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
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final FeedbackRepository feedbackRepository;

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
        boolean isPublished = submission.getPublishedAt() != null;
        boolean isGraded = submission.getGradedAt() != null;

        if (isPublished) {
            throw new ConflictException("Submission result has already been published.");
        }
        if (isGraded) {
            throw new ConflictException("Submission result has already been graded.");
        }
        if (status == SubmissionStatus.MISSED || status == SubmissionStatus.NOT_SUBMITTED) {
            throw new BadRequestException("Cannot grade because the student missed the submission.");
        }
        if (status != SubmissionStatus.SUBMITTED) {
            throw new BadRequestException("Submission result cannot be graded because it is not in submitted status.");
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

        boolean isPublished = submission.getPublishedAt() != null;

        if (!isPublished) {
            throw new NotFoundException("Submission result is not available yet.");
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
                .filter(s -> s.getPublishedAt() != null)
                .map(submission -> {
                    UserResponse userResponse = Optional
                            .ofNullable(userClient.getUserInfoById(submission.getStudentId()).getBody())
                            .map(APIResponse::getPayload)
                            .orElseThrow(() -> new NotFoundException(
                                    "Student user info not found: " + submission.getStudentId()
                            ));

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

        Assessment assessment = assessmentRepository
                .findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException(
                        "Assessment with ID " + assessmentId + " not found"
                ));

        boolean hasAnyToPublish = assessment.getSubmissions().stream()
                .anyMatch(s ->
                        s.getPublishedAt() == null &&
                        (s.getGradedAt() != null || s.getStatus() == SubmissionStatus.MISSED)
                );

        if (!hasAnyToPublish) {
            throw new ConflictException("No submissions available to publish.");
        }

        Instant now = Instant.now();

        for (Submission submission : assessment.getSubmissions()) {

            if (submission.getPublishedAt() != null) continue;

            SubmissionStatus status = submission.getStatus();
            boolean isGraded = submission.getGradedAt() != null;

            if (isGraded) {
                submission.setPublishedAt(now);
                continue;
            }

            if (status == SubmissionStatus.MISSED) {

                List<Question> questions = submission.getAssessment().getQuestions();

                BigDecimal maxScore = questions.stream()
                        .map(Question::getPoints)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                submission.setMaxScore(maxScore);
                submission.setPublishedAt(now);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseResultSummary getMySubmissionResultSummary(TimeRange range, SubmissionSort sort) {

        UUID studentId = extractCurrentUserId();

        List<Submission> all = submissionRepository.findAllByStudentIdAndSubmittedAtIsNotNull(studentId);

        if (all.isEmpty()) {
            return StudentResponseResultSummary.builder()
                    .scoreEarned(BigDecimal.ZERO)
                    .maxScore(BigDecimal.ZERO)
                    .totalFeedbacks(0L)
                    .build();
        }

        Submission anchor = pickAnchor(all, sort);

        ZoneId zone = resolveZoneFromSubmission(anchor);

        Instant[] r = resolveRangeInstant(range, zone);
        Instant start = r[0];
        Instant end = r[1];

        PageRequest one = PageRequest.of(0, 1, toSort(sort));

        List<Submission> picked =
                submissionRepository.findAllByStudentIdAndSubmittedAtIsNotNullAndPublishedAtIsNotNullAndSubmittedAtBetween(
                        studentId, start, end, one
                );

        if (picked.isEmpty()) {
            return StudentResponseResultSummary.builder()
                    .scoreEarned(BigDecimal.ZERO)
                    .maxScore(BigDecimal.ZERO)
                    .totalFeedbacks(0L)
                    .build();
        }

        Submission submission = picked.getFirst();

        long totalFeedbacks = feedbackRepository.countFeedbacksBySubmissionId(submission.getSubmissionId());

        return StudentResponseResultSummary.builder()
                .scoreEarned(nz(submission.getScoreEarned()))
                .maxScore(nz(submission.getMaxScore()))
                .totalFeedbacks(totalFeedbacks)
                .build();
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

    private Sort toSort(SubmissionSort sort) {
        if (sort == null) sort = SubmissionSort.LATEST_WORK;

        return switch (sort) {
            case LATEST_WORK -> Sort.by(Sort.Direction.DESC, "submittedAt");
            case OLDEST_WORK -> Sort.by(Sort.Direction.ASC, "submittedAt");
            case HIGHEST_SCORE -> Sort.by(Sort.Direction.DESC, "scoreEarned");
        };
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private Instant nzInstant(Instant v) {
        return v == null ? Instant.EPOCH : v;
    }

    private Submission pickAnchor(List<Submission> all, SubmissionSort sort) {
        if (sort == null) sort = SubmissionSort.LATEST_WORK;

        return switch (sort) {
            case LATEST_WORK -> all.stream()
                    .max((a, b) -> nzInstant(a.getSubmittedAt()).compareTo(nzInstant(b.getSubmittedAt())))
                    .orElse(all.getFirst());

            case OLDEST_WORK -> all.stream()
                    .min((a, b) -> nzInstant(a.getSubmittedAt()).compareTo(nzInstant(b.getSubmittedAt())))
                    .orElse(all.getFirst());

            case HIGHEST_SCORE -> all.stream()
                    .max((a, b) -> nz(a.getScoreEarned()).compareTo(nz(b.getScoreEarned())))
                    .orElse(all.getFirst());
        };
    }

    private ZoneId resolveZoneFromSubmission(Submission submission) {
        String tz = submission.getTimeZone();
        if (tz == null || tz.isBlank()) return ZoneId.of("UTC");

        try {
            return ZoneId.of(tz.trim());
        } catch (DateTimeException e) {
            return ZoneId.of("UTC");
        }
    }

    private Instant[] resolveRangeInstant(TimeRange range, ZoneId zone) {
        ZonedDateTime now = ZonedDateTime.now(zone);

        return switch (range) {
            case CURRENT_MONTH -> new Instant[]{
                    now.withDayOfMonth(1).toLocalDate().atStartOfDay(zone).toInstant(),
                    now.toInstant()
            };
            case LAST_MONTH -> {
                ZonedDateTime start = now.minusMonths(1)
                        .withDayOfMonth(1)
                        .toLocalDate()
                        .atStartOfDay(zone);

                ZonedDateTime end = start.plusMonths(1);
                yield new Instant[]{start.toInstant(), end.toInstant()};
            }
            case THIS_YEAR -> new Instant[]{
                    now.withDayOfYear(1).toLocalDate().atStartOfDay(zone).toInstant(),
                    now.toInstant()
            };
        };
    }

}
