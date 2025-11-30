package kr.com.mfa.mfaphase1api.job;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AssessmentFinishJob implements org.quartz.Job {

    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        String idStr = context.getMergedJobDataMap().getString("assessmentId");
        UUID assessmentId = UUID.fromString(idStr);

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow();

        if (assessment.getStatus() == AssessmentStatus.STARTED
            || assessment.getStatus() == AssessmentStatus.SCHEDULED) {
            assessment.setStatus(AssessmentStatus.FINISHED);
            assessmentRepository.save(assessment);
        }
    }
}
