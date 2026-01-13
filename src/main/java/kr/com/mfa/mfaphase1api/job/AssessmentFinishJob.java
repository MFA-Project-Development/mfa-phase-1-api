package kr.com.mfa.mfaphase1api.job;

import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentMessage;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AnswerRepository;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionRepository;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        String idStr = context.getMergedJobDataMap().getString("assessmentId");
        UUID assessmentId = UUID.fromString(idStr);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow();

        List<UUID> allStudentIds =
                assessment.getClassSubSubjectInstructor()
                        .getClassSubSubject()
                        .getClazz()
                        .getStudentClassEnrollments()
                        .stream()
                        .map(StudentClassEnrollment::getStudentId)
                        .toList();

        Set<UUID> submittedStudentIds =
                submissionRepository.findAllByAssessment_AssessmentId(assessmentId)
                        .stream()
                        .map(Submission::getStudentId)
                        .collect(Collectors.toSet());

        List<UUID> missingStudentIds = allStudentIds.stream()
                .filter(studentId -> !submittedStudentIds.contains(studentId))
                .toList();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(assessment.getTimeZone()));
        Instant nowInstant = now.toInstant();

        List<Question> questions = questionRepository.findAllByAssessment_AssessmentId(assessmentId);

        for (UUID studentId : missingStudentIds) {

            Submission submission = Submission.builder()
                    .status(SubmissionStatus.MISSED)
                    .maxScore(BigDecimal.ZERO)
                    .scoreEarned(BigDecimal.ZERO)
                    .assessment(assessment)
                    .studentId(studentId)
                    .startedAt(nowInstant)
                    .submittedAt(nowInstant)
                    .timeZone(assessment.getTimeZone())
                    .build();

            submissionRepository.save(submission);

            List<Answer> answers = questions.stream()
                    .map(q -> Answer.builder()
                            .pointsAwarded(BigDecimal.ZERO)
                            .question(q)
                            .submission(submission)
                            .build())
                    .toList();

            answerRepository.saveAll(answers);
        }


        if (assessment.getStatus() == AssessmentStatus.STARTED
            || assessment.getStatus() == AssessmentStatus.SCHEDULED) {
            assessment.setStatus(AssessmentStatus.FINISHED);
            Assessment saved = assessmentRepository.save(assessment);
            AssessmentMessage message = new AssessmentMessage(
                    saved.getAssessmentId().toString(),
                    saved.getTitle(),
                    saved.getDescription(),
                    saved.getStartDate().toString(),
                    saved.getDueDate().toString(),
                    saved.getTimeLimit().toString(),
                    saved.getStatus().toString(),
                    saved.getAssessmentType().toString(),
                    saved.getClassSubSubjectInstructor().getClassSubSubject().getClazz().getClassId().toString()
            );
            socketIoClientService.emitAssessmentStatus(message);
        }
    }
}
