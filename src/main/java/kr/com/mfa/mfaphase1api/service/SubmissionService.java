package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.PaperResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {
    UUID startSubmission(UUID assessmentId);

    void finalizeSubmission(UUID assessmentId, UUID submissionId);

    void persistSubmissionPapers(UUID assessmentId, UUID submissionId, List<String> fileNames);

    List<PaperResponse> getSubmissionPapers(UUID assessmentId, UUID submissionId);

}
