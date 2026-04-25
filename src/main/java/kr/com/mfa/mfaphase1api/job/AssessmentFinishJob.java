package kr.com.mfa.mfaphase1api.job;

import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentMessage;
import kr.com.mfa.mfaphase1api.model.entity.Answer;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AnswerRepository;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionRepository;
import kr.com.mfa.mfaphase1api.repository.StudentClassEnrollmentRepository;
import kr.com.mfa.mfaphase1api.repository.SubmissionRepository;
import kr.com.mfa.mfaphase1api.service.SocketIoClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentFinishJob implements org.quartz.Job {

    private final AssessmentRepository assessmentRepository;
    private final SocketIoClientService socketIoClientService;
    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final StudentClassEnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        String idStr = context.getMergedJobDataMap().getString("assessmentId");
        UUID assessmentId = UUID.fromString(idStr);

        Assessment assessment = assessmentRepository.findByIdWithClassInfo(assessmentId)
                .orElseThrow(() -> new IllegalStateException("Assessment not found: " + assessmentId));

        // Single projection query for student IDs — no lazy collection traversal
        UUID classId = assessment.getClassSubSubjectInstructor()
                .getClassSubSubject()
                .getClazz()
                .getClassId();
        List<UUID> allStudentIds = enrollmentRepository.findStudentIdsByClassId(classId);

        // Single projection query — no full entity load needed
        Set<UUID> submittedStudentIds = new HashSet<>(
                submissionRepository.findStudentIdsByAssessmentId(assessmentId)
        );

        List<UUID> missingStudentIds = allStudentIds.stream()
                .filter(id -> !submittedStudentIds.contains(id))
                .toList();

        if (!missingStudentIds.isEmpty()) {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(assessment.getTimeZone()));
            Instant nowInstant = now.toInstant();

            List<Question> questions = questionRepository.findAllByAssessment_AssessmentId(assessmentId);

            // Build and batch-save all MISSED submissions in one round-trip
            List<Submission> missedSubmissions = missingStudentIds.stream()
                    .map(studentId -> Submission.builder()
                            .status(SubmissionStatus.MISSED)
                            .maxScore(BigDecimal.ZERO)
                            .scoreEarned(BigDecimal.ZERO)
                            .assessment(assessment)
                            .studentId(studentId)
                            .startedAt(nowInstant)
                            .submittedAt(nowInstant)
                            .timeZone(assessment.getTimeZone())
                            .build())
                    .toList();

            List<Submission> savedSubmissions = submissionRepository.saveAll(missedSubmissions);

            // Build and batch-save all answers across all submissions in one round-trip
            List<Answer> allAnswers = savedSubmissions.stream()
                    .flatMap(submission -> questions.stream()
                            .map(q -> Answer.builder()
                                    .pointsAwarded(BigDecimal.ZERO)
                                    .question(q)
                                    .submission(submission)
                                    .build()))
                    .collect(Collectors.toList());

            answerRepository.saveAll(allAnswers);
            log.info("Created {} MISSED submissions for assessment {}", savedSubmissions.size(), assessmentId);
        }

        if (assessment.getStatus() == AssessmentStatus.STARTED
                || assessment.getStatus() == AssessmentStatus.SCHEDULED) {
            assessment.setStatus(AssessmentStatus.FINISHED);
            Assessment saved = assessmentRepository.save(assessment);

            String savedClassId = saved.getClassSubSubjectInstructor()
                    .getClassSubSubject()
                    .getClazz()
                    .getClassId()
                    .toString();

            AssessmentMessage message = new AssessmentMessage(
                    saved.getAssessmentId().toString(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getStartDate().toString(),
                    saved.getDueDate().toString(),
                    saved.getTimeLimit().toString(),
                    saved.getStatus().toString(),
                    saved.getAssessmentType().toString(),
                    savedClassId
            );
            socketIoClientService.emitAssessmentStatus(message);
            log.info("Assessment {} transitioned to FINISHED", assessmentId);
        }
    }
}
