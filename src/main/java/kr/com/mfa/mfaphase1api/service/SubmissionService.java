package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PaperResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface SubmissionService {
    
    Object startSubmission(UUID assessmentId);

    void finalizeSubmission(UUID assessmentId, UUID submissionId);

    void persistSubmissionPapers(UUID assessmentId, UUID submissionId, List<String> fileNames);

    List<PaperResponse> getSubmissionPapers(UUID assessmentId, UUID submissionId);

    void deleteSubmission(UUID assessmentId,UUID submissionId);

    void saveSubmission(UUID assessmentId, UUID submissionId);

    PagedResponse<List<SubmissionResponse>> getAllSubmissions(UUID assessmentId, Integer page, Integer size, SubmissionProperty property, Sort.Direction direction);
}
