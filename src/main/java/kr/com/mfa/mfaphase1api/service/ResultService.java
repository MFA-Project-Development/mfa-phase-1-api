package kr.com.mfa.mfaphase1api.service;


import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ResultService {
    void publishSubmissionResult(UUID assessmentId, UUID submissionId);

    SubmissionResponse getSubmissionResult(UUID assessmentId, UUID submissionId);

    PagedResponse<List<SubmissionResponse>> getAllSubmissionResults(UUID assessmentId, Integer page, Integer size, SubmissionProperty property, Sort.Direction direction);
}
