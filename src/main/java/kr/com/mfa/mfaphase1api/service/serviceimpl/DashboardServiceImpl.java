package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.*;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.DashboardService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserClient userClient;
    private final SubSubjectRepository subSubjectRepository;
    private final ClassRepository classRepository;
    private final AssessmentRepository assessmentRepository;
    private final MotivationContentRepository motivationContentRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getAdminOverview() {

        long totalInstructors = getUserCountByRole(BaseRole.ROLE_INSTRUCTOR);
        long totalStudents = getUserCountByRole(BaseRole.ROLE_STUDENT);
        long totalSubSubjects = subSubjectRepository.count();
        long totalClasses = classRepository.count();

        return AdminOverviewResponse.builder()
                .totalInstructors(totalInstructors)
                .totalStudents(totalStudents)
                .totalClasses(totalClasses)
                .totalSubSubjects(totalSubSubjects)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentOverviewResponse getStudentOverview(Month month, PerformanceStatus performanceStatus) {

        UUID currentUserId = extractCurrentUserId();

        ZoneId zone = ZoneId.of("UTC");
        int year = Year.now(zone).getValue();

        YearMonth ym = (month == null)
                ? YearMonth.now(zone)
                : YearMonth.of(year, month);

        Instant start = ym.atDay(1).atStartOfDay(zone).toInstant();
        Instant endExclusive = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        Submission submission = submissionRepository
                .findFirstByStudentIdAndSubmittedAtBetweenOrderBySubmittedAtDesc(currentUserId, start, endExclusive)
                .orElse(null);

        List<Assessment> assessments = assessmentRepository
                .findAssessmentsInMonthByStudent(currentUserId, start, endExclusive);

        List<Submission> submissions = submissionRepository.findAllSubmissionsInMonth(currentUserId, start, endExclusive);

        List<Submission> submissionsLastTwoMonths = submissionRepository
                .findLastSubmissionOfTwoMonthsEndingAt(currentUserId, endExclusive);

        BigDecimal score = (submission != null && submission.getScoreEarned() != null)
                ? submission.getScoreEarned()
                : BigDecimal.ZERO;

        ScoreStatus scoreStatus;
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            scoreStatus = ScoreStatus.GOOD;
        } else if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
            scoreStatus = ScoreStatus.AVERAGE;
        } else {
            scoreStatus = ScoreStatus.LOW;
        }

        BigDecimal progressPercent = BigDecimal.valueOf(100);
        BigDecimal progressChange = BigDecimal.ZERO;

        if (month == null || submission != null) {
            if (submissionsLastTwoMonths.size() >= 2) {
                Submission current = submissionsLastTwoMonths.get(0);
                Submission previous = submissionsLastTwoMonths.get(1);

                if (current.getSubmittedAt() != null && previous.getSubmittedAt() != null
                    && current.getMaxScore() != null && previous.getMaxScore() != null
                    && current.getMaxScore().compareTo(BigDecimal.ZERO) > 0
                    && previous.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {

                    BigDecimal currentEarned = current.getScoreEarned() != null ? current.getScoreEarned() : BigDecimal.ZERO;
                    BigDecimal previousEarned = previous.getScoreEarned() != null ? previous.getScoreEarned() : BigDecimal.ZERO;

                    BigDecimal currentPercent = currentEarned
                            .multiply(BigDecimal.valueOf(100))
                            .divide(current.getMaxScore(), 2, RoundingMode.HALF_UP);

                    BigDecimal previousPercent = previousEarned
                            .multiply(BigDecimal.valueOf(100))
                            .divide(previous.getMaxScore(), 2, RoundingMode.HALF_UP);

                    progressChange = currentPercent
                            .subtract(previousPercent)
                            .setScale(2, RoundingMode.HALF_UP);

                    progressPercent = currentPercent.min(BigDecimal.valueOf(100));
                }
            }
        } else {
            progressPercent = null;
            progressChange = null;
        }

        BigDecimal average = null;
        AverageStatus averageStatus = null;

        if (submissions != null && !submissions.isEmpty()) {
            BigDecimal totalPercent = BigDecimal.ZERO;
            int valid = 0;

            for (Submission s : submissions) {
                if (s.getMaxScore() != null
                    && s.getMaxScore().compareTo(BigDecimal.ZERO) > 0
                    && s.getScoreEarned() != null) {

                    BigDecimal percent = s.getScoreEarned()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(s.getMaxScore(), 2, RoundingMode.HALF_UP);

                    totalPercent = totalPercent.add(percent);
                    valid++;
                }
            }

            if (valid > 0) {
                average = totalPercent.divide(BigDecimal.valueOf(valid), 2, RoundingMode.HALF_UP);

                if (average.compareTo(BigDecimal.valueOf(80)) >= 0) {
                    averageStatus = AverageStatus.HIGH;
                } else if (average.compareTo(BigDecimal.valueOf(50)) >= 0) {
                    averageStatus = AverageStatus.MEDIUM;
                } else {
                    averageStatus = AverageStatus.LOW;
                }
            }
        }

        PerformanceStatus effectiveStatus =
                (performanceStatus == null)
                        ? PerformanceStatus.TOTAL_AVG_SCORE
                        : performanceStatus;

        Month responseMonth = (month != null) ? month : ym.getMonth();
        List<PerformanceItem> performance;

        if (effectiveStatus == PerformanceStatus.TOTAL_ASSESSMENT) {
            performance = List.of(
                    PerformanceItem.builder()
                            .month(responseMonth)
                            .totalAssessment((long) (assessments == null ? 0 : assessments.size()))
                            .build()
            );
        } else {
            performance = List.of(
                    PerformanceItem.builder()
                            .month(responseMonth)
                            .totalAvgScore(average == null ? BigDecimal.ZERO : average)
                            .build()
            );
        }


        List<Submission> top5 = submissionRepository
                .findTop5ByStudentIdAndPublishedAtIsNotNullAndStatusOrderByPublishedAtDesc(currentUserId, SubmissionStatus.SUBMITTED);

        // Batch fetch graders in one call instead of one Feign call per submission
        List<UUID> gradedByIds = top5.stream()
                .map(Submission::getGradedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> gradedByNames = resolveGradedByBatch(gradedByIds);

        List<RecentFeedbackAssessmentResponse> recentFeedbackAssessments = top5.stream()
                .map(s -> RecentFeedbackAssessmentResponse.builder()
                        .assessmentId(s.getAssessment().getAssessmentId())
                        .submissionId(s.getSubmissionId())
                        .title(s.getAssessment().getTitle())
                        .gradedBy(s.getGradedBy() != null ? gradedByNames.get(s.getGradedBy()) : null)
                        .publishedAt(
                                s.getPublishedAt() != null
                                        ? LocalDateTime.ofInstant(s.getPublishedAt(), ZoneId.of(s.getTimeZone()))
                                        : null
                        )
                        .build()
                )
                .toList();

        return StudentOverviewResponse.builder()
                .score(score)
                .scoreStatus(scoreStatus)
                .progressPercent(progressPercent)
                .progressChange(progressChange)
                .average(average)
                .averageStatus(averageStatus)
                .performance(performance)
                .recentFeedbackAssessments(recentFeedbackAssessments)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public TeacherOverviewResponse getInstructorOverview(Month month, UUID classId) {

        UUID currentUserId = extractCurrentUserId();

        List<Class> classes = (classId == null)
                ? classRepository.findAllByClassSubSubjects_ClassSubSubjectInstructors_InstructorId(currentUserId)
                : classRepository.findAllByClassIdAndClassSubSubjects_ClassSubSubjectInstructors_InstructorId(
                classId, currentUserId
        );

        // Batch count query — avoids N lazy-load queries on studentClassEnrollments
        List<UUID> classIds = classes.stream().map(Class::getClassId).toList();
        Map<UUID, Long> studentCountByClass = classIds.isEmpty() ? Map.of() :
                classRepository.countStudentsByClassIds(classIds).stream()
                        .collect(Collectors.toMap(
                                row -> (UUID) row[0],
                                row -> (Long) row[1]
                        ));

        long totalStudents = studentCountByClass.values().stream().mapToLong(Long::longValue).sum();

        List<ClassStudentCount> classStudentCounts = new ArrayList<>();
        if (totalStudents == 0) {
            for (Class c : classes) {
                classStudentCounts.add(ClassStudentCount.builder()
                        .classId(c.getClassId())
                        .className(c.getName())
                        .studentCount(0)
                        .percentage(0)
                        .build());
            }
        } else {
            int used = 0;

            for (Class c : classes) {
                int count = studentCountByClass.getOrDefault(c.getClassId(), 0L).intValue();
                int percent = (int) ((double) count * 100 / totalStudents);

                used += percent;

                classStudentCounts.add(ClassStudentCount.builder()
                        .classId(c.getClassId())
                        .className(c.getName())
                        .studentCount(count)
                        .percentage(percent)
                        .build());
            }

            int remaining = 100 - used;
            for (int i = 0; i < remaining; i++) {
                ClassStudentCount item = classStudentCounts.get(i % classStudentCounts.size());
                item.setPercentage(item.getPercentage() + 1);
            }
        }

        // Count motivation content once — outside the class loop below
        long totalMotivation = motivationContentRepository.countByCreatedBy(currentUserId);

        List<Assessment> assessments;

        if (month == null) {
            assessments = (classId == null)
                    ? assessmentRepository.findAllByCreatedBy_AndStatus(
                    currentUserId, AssessmentStatus.FINISHED
            )
                    : assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(
                    currentUserId, AssessmentStatus.FINISHED, classId
            );
        } else {
            int year = LocalDate.now().getYear();
            YearMonth ym = YearMonth.of(year, month);

            LocalDateTime startDate = ym.atDay(1).atStartOfDay();
            LocalDateTime endDate = ym.plusMonths(1).atDay(1).atStartOfDay();

            ZoneId zone = ZoneId.of("UTC");

            Instant newStartDate = startDate
                    .atZone(zone)
                    .toInstant();

            Instant newEndDate = endDate
                    .atZone(zone)
                    .toInstant();

            assessments = (classId == null)
                    ? assessmentRepository.findAllByCreatedBy_AndStatus_AndStartDateBetween(
                    currentUserId, AssessmentStatus.FINISHED, newStartDate, newEndDate
            )
                    : assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndStartDateBetween(
                    currentUserId, AssessmentStatus.FINISHED, classId, newStartDate, newEndDate
            );
        }

        int exams = 0, quizzes = 0, assignments = 0, homeworks = 0;

        for (Assessment assessment : assessments) {
            switch (assessment.getAssessmentType()) {
                case EXAM -> exams++;
                case QUIZ -> quizzes++;
                case ASSIGNMENT -> assignments++;
                case HOMEWORK -> homeworks++;
            }
        }

        List<AssessmentSummaryByClass> assessmentSummaryByClasses = new ArrayList<>();
        RecentActivity recentActivity = new RecentActivity();
        long totalPendingGradingAll = 0, totalUpcomingAssessmentAll = 0;

        for (Class clazz : classes) {

            List<Assessment> assessmentsByClass;

            if (month == null) {
                assessmentsByClass = assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(
                        currentUserId, AssessmentStatus.FINISHED, clazz.getClassId()
                );
            } else {
                int year = LocalDate.now().getYear();
                YearMonth ym = YearMonth.of(year, month);

                LocalDateTime startDate = ym.atDay(1).atStartOfDay();
                LocalDateTime endDate = ym.plusMonths(1).atDay(1).atStartOfDay();

                ZoneId zone = ZoneId.of("UTC");

                Instant newStartDate = startDate.atZone(zone).toInstant();
                Instant newEndDate = endDate.atZone(zone).toInstant();

                assessmentsByClass = assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndStartDateBetween(
                        currentUserId, AssessmentStatus.FINISHED, clazz.getClassId(), newStartDate, newEndDate);
            }

            BigDecimal examAvg = BigDecimal.ZERO;
            BigDecimal quizAvg = BigDecimal.ZERO;
            BigDecimal assignmentAvg = BigDecimal.ZERO;
            BigDecimal homeworkAvg = BigDecimal.ZERO;
            long totalPendingGrading = 0, totalUpcomingAssessment = 0;

            // Batch-fetch submission summaries for all assessments in this class — avoids N+1
            List<UUID> assessmentIds = assessmentsByClass.stream()
                    .map(Assessment::getAssessmentId)
                    .toList();
            Map<UUID, List<SubmissionRepository.SubmissionSummary>> summariesByAssessment =
                    assessmentIds.isEmpty() ? Map.of() :
                            submissionRepository.findSummariesByAssessmentIds(assessmentIds).stream()
                                    .collect(Collectors.groupingBy(SubmissionRepository.SubmissionSummary::getAssessmentId));

            for (Assessment assessment : assessmentsByClass) {

                Instant startAt = assessment.getStartDate();
                if (startAt == null) continue;

                ZoneId zone = ZoneId.of(assessment.getTimeZone());
                ZonedDateTime nowZdt = ZonedDateTime.now(zone);

                ZonedDateTime weekEndZdt = nowZdt
                        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                        .toLocalDate()
                        .atStartOfDay(zone);

                Instant now = nowZdt.toInstant();
                Instant weekEnd = weekEndZdt.toInstant();

                if (startAt.isBefore(now) && startAt.isBefore(weekEnd)) {
                    totalUpcomingAssessment++;
                }

                List<SubmissionRepository.SubmissionSummary> summaries =
                        summariesByAssessment.getOrDefault(assessment.getAssessmentId(), List.of());
                if (summaries.isEmpty()) continue;

                BigDecimal totalStudentScore = BigDecimal.ZERO;

                for (SubmissionRepository.SubmissionSummary summary : summaries) {
                    if (summary.getScoreEarned() != null) {
                        totalStudentScore = totalStudentScore.add(summary.getScoreEarned());
                    }
                    if (summary.getGradedAt() == null) {
                        totalPendingGrading++;
                    }
                }

                BigDecimal avgScore = totalStudentScore.divide(
                        BigDecimal.valueOf(summaries.size()),
                        2,
                        RoundingMode.HALF_UP
                );

                switch (assessment.getAssessmentType()) {
                    case EXAM -> examAvg = avgScore;
                    case QUIZ -> quizAvg = avgScore;
                    case ASSIGNMENT -> assignmentAvg = avgScore;
                    case HOMEWORK -> homeworkAvg = avgScore;
                }
            }

            assessmentSummaryByClasses.add(AssessmentSummaryByClass.builder()
                    .classId(clazz.getClassId())
                    .className(clazz.getName())
                    .exams(examAvg)
                    .assignments(assignmentAvg)
                    .quizzes(quizAvg)
                    .homeworks(homeworkAvg)
                    .build());

            totalPendingGradingAll += totalPendingGrading;
            totalUpcomingAssessmentAll += totalUpcomingAssessment;
        }

        // totalMotivation is fetched once above the loop — set after the loop
        recentActivity.setTotalPendingGrading(totalPendingGradingAll);
        recentActivity.setTotalUpcomingAssessment(totalUpcomingAssessmentAll);
        recentActivity.setTotalMotivation(totalMotivation);


        return TeacherOverviewResponse.builder()
                .totalStudentSummary(TotalStudentSummary.builder()
                        .total(totalStudents)
                        .byClass(classStudentCounts)
                        .build())
                .teachingSummary(TeachingSummary.builder()
                        .exams(exams)
                        .assignments(assignments)
                        .quizzes(quizzes)
                        .homeworks(homeworks)
                        .build())
                .avgStudentScoreByClasses(assessmentSummaryByClasses)
                .recentActivity(recentActivity)
                .build();
    }

    private long getUserCountByRole(BaseRole role) {
        return Optional.ofNullable(userClient.getAllUsersBaseRole(role))
                .map(ResponseEntity::getBody)
                .map(APIResponse::getPayload)
                .map(List::size)
                .map(Integer::longValue)
                .orElse(0L);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private Map<UUID, String> resolveGradedByBatch(List<UUID> userIds) {
        if (userIds.isEmpty()) return Map.of();
        try {
            List<UserResponse> users = Objects.requireNonNull(
                    userClient.getAllUserByUserIds(
                            new kr.com.mfa.mfaphase1api.model.dto.request.UserIdsRequest(userIds)
                    ).getBody()
            ).getPayload();
            return users.stream().collect(Collectors.toMap(
                    UserResponse::getUserId,
                    u -> ((u.getFirstName() != null ? u.getFirstName() : "") + " " +
                          (u.getLastName() != null ? u.getLastName() : "")).trim()
            ));
        } catch (Exception e) {
            log.warn("Batch graded-by user fetch failed: {}", e.getMessage());
            return Map.of();
        }
    }


}
