package kr.com.mfa.mfaphase1api.job;

import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentMessage;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.service.SocketIoClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentStartJob implements org.quartz.Job {

    private final AssessmentRepository assessmentRepository;
    private final SocketIoClientService socketIoClientService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        String idStr = context.getMergedJobDataMap().getString("assessmentId");
        UUID assessmentId = UUID.fromString(idStr);

        Assessment assessment = assessmentRepository.findByIdWithClassInfo(assessmentId)
                .orElseThrow(() -> new IllegalStateException("Assessment not found: " + assessmentId));

        if (assessment.getStatus() == AssessmentStatus.SCHEDULED) {
            assessment.setStatus(AssessmentStatus.STARTED);
            Assessment saved = assessmentRepository.save(assessment);

            String classId = saved.getClassSubSubjectInstructor()
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
                    classId
            );
            socketIoClientService.emitAssessmentStatus(message);
            log.info("Assessment {} transitioned to STARTED", assessmentId);
        } else {
            log.warn("AssessmentStartJob skipped: assessment {} has status {}", assessmentId, assessment.getStatus());
        }
    }
}
