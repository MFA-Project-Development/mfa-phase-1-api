package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.job.AssessmentFinishJob;
import kr.com.mfa.mfaphase1api.job.AssessmentStartJob;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.service.QuartzSchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class QuartzSchedulerServiceImpl implements QuartzSchedulerService {

    private final Scheduler scheduler;

    @Override
    public void scheduleStartAndFinishJobs(Assessment assessment) {
        scheduleStartJob(assessment);
        scheduleFinishJob(assessment);
    }

    private void scheduleStartJob(Assessment assessment) {
        JobDetail jobDetail = JobBuilder.newJob(AssessmentStartJob.class)
                .withIdentity("assessment-start-" + assessment.getAssessmentId(), "assessment")
                .usingJobData("assessmentId", assessment.getAssessmentId().toString())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("assessment-start-trigger-" + assessment.getAssessmentId(), "assessment")
                .startAt(Date.from(assessment.getStartDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule assessment start job", e);
        }
    }

    private void scheduleFinishJob(Assessment assessment) {
        JobDetail jobDetail = JobBuilder.newJob(AssessmentFinishJob.class)
                .withIdentity("assessment-finish-" + assessment.getAssessmentId(), "assessment")
                .usingJobData("assessmentId", assessment.getAssessmentId().toString())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("assessment-finish-trigger-" + assessment.getAssessmentId(), "assessment")
                .startAt(Date.from(assessment.getDueDate()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()))
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule assessment finish job", e);
        }
    }


}
