package kr.com.mfa.mfaphase1api.service;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {
    UUID startSubmission(UUID assessmentId);

    void finalizeSubmission(UUID assessmentId, UUID submissionId);

    void persistSubmissionPapers(UUID assessmentId, UUID submissionId, List<String> fileNames);
}
