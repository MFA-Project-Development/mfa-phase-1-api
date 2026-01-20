package kr.com.mfa.mfaphase1api.service;


import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentResponseResultSummary;
import kr.com.mfa.mfaphase1api.model.dto.response.SubmissionResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionProperty;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionSort;
import kr.com.mfa.mfaphase1api.model.enums.TimeRange;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ResultService {
    void gradeSubmissionResult(UUID assessmentId, UUID submissionId);

    SubmissionResponse getSubmissionResult(UUID assessmentId, UUID submissionId);

    PagedResponse<List<SubmissionResponse>> getAllSubmissionResults(UUID assessmentId, Integer page, Integer size, SubmissionProperty property, Sort.Direction direction);

    void publishSubmissionResult(UUID assessmentId);

    StudentResponseResultSummary getMySubmissionResultSummary(TimeRange range, SubmissionSort sort);
}
