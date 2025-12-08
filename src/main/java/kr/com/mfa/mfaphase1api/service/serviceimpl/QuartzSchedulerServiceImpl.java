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
        scheduleOrRescheduleStartJob(assessment);
        scheduleOrRescheduleFinishJob(assessment);
    }

    private void scheduleOrRescheduleStartJob(Assessment assessment) {
        String jobName = "assessment-start-" + assessment.getAssessmentId();
        String triggerName = "assessment-start-trigger-" + assessment.getAssessmentId();
        String group = "assessment";

        JobDetail jobDetail = JobBuilder.newJob(AssessmentStartJob.class)
                .withIdentity(jobName, group)
                .usingJobData("assessmentId", assessment.getAssessmentId().toString())
                .build();

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, group);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(Date.from(
                        assessment.getStartDate()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                ))
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            if (scheduler.checkExists(triggerKey)) {
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to (re)schedule assessment start job", e);
        }
    }

    private void scheduleOrRescheduleFinishJob(Assessment assessment) {
        String jobName = "assessment-finish-" + assessment.getAssessmentId();
        String triggerName = "assessment-finish-trigger-" + assessment.getAssessmentId();
        String group = "assessment";

        JobDetail jobDetail = JobBuilder.newJob(AssessmentFinishJob.class)
                .withIdentity(jobName, group)
                .usingJobData("assessmentId", assessment.getAssessmentId().toString())
                .build();

        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, group);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .startAt(Date.from(
                        assessment.getDueDate()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                ))
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        try {
            if (scheduler.checkExists(triggerKey)) {
                scheduler.rescheduleJob(triggerKey, trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to (re)schedule assessment finish job", e);
        }
    }
}
