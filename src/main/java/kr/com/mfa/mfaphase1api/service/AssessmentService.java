package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    AssessmentResponse createAssessment(AssessmentRequest request);

    PagedResponse<List<AssessmentResponse>> getAllAssessments(Integer page, Integer size, AssessmentProperty property, Sort.Direction direction);

    AssessmentResponse getAssessmentById(UUID assessmentId);

    AssessmentResponse updateAssessmentById(UUID assessmentId, AssessmentRequest request);

    void deleteAssessmentById(UUID assessmentId);

}
