package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Class;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentType;
import kr.com.mfa.mfaphase1api.model.enums.BaseRole;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.ClassRepository;
import kr.com.mfa.mfaphase1api.repository.SubSubjectRepository;
import kr.com.mfa.mfaphase1api.service.DashboardService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
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
    public StudentOverviewResponse getStudentOverview(Month month, String subSubjectName) {
        return null;
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

        long totalStudents = classes.stream()
                .mapToLong(c -> c.getStudentClassEnrollments().size())
                .sum();

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
                int count = c.getStudentClassEnrollments().size();
                int percent = (int) ((double) count * 100 / totalStudents); // floor

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

        List<Assessment> assessments;

        if (month == null) {
            assessments = (classId == null)
                    ? assessmentRepository.findAllByCreatedBy_AndStatus(
                    currentUserId, AssessmentStatus.STARTED
            )
                    : assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId(
                    currentUserId, AssessmentStatus.STARTED, classId
            );
        } else {
            int year = LocalDate.now().getYear();
            YearMonth ym = YearMonth.of(year, month);

            LocalDateTime startDate = ym.atDay(1).atStartOfDay();
            LocalDateTime endDate = ym.plusMonths(1).atDay(1).atStartOfDay();

            assessments = (classId == null)
                    ? assessmentRepository.findAllByCreatedBy_AndStatus_AndStartDateBetween(
                    currentUserId, AssessmentStatus.STARTED, startDate, endDate
            )
                    : assessmentRepository.findAllByCreatedBy_AndStatus_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_ClassId_AndStartDateBetween(
                    currentUserId, AssessmentStatus.STARTED, classId, startDate, endDate
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

}
