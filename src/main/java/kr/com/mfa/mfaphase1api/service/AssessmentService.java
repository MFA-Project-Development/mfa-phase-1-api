package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ResourceRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    AssessmentResponse createAssessment(UUID classId, AssessmentRequest request);

    PagedResponse<List<AssessmentResponse>> getAllAssessments(UUID classId, Integer page, Integer size, AssessmentProperty property, Sort.Direction direction);

    AssessmentResponse getAssessmentById(UUID classId, UUID assessmentId);

    AssessmentResponse updateAssessmentById(UUID classId, UUID assessmentId, AssessmentRequest request);

    void deleteAssessmentById(UUID classId, UUID assessmentId);

    void persistAssessmentResource(UUID classId, UUID assessmentId, ResourceKind kind, List<ResourceRequest> requests);
}
