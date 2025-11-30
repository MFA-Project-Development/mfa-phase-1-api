package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.entity.Assessment;

public interface QuartzSchedulerService {

    void scheduleStartAndFinishJobs(Assessment assessment);

}
