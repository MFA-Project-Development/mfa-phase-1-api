package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentScheduleRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ResourceRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentProperty;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    AssessmentResponse createAssessment(UUID classId, AssessmentRequest request);

    PagedResponse<List<AssessmentResponse>> getAllAssessmentsByClassId(UUID classId, Integer page, Integer size, AssessmentProperty property, Sort.Direction direction);

    AssessmentResponse getAssessmentById(UUID classId, UUID assessmentId);

    AssessmentResponse updateAssessmentById(UUID classId, UUID assessmentId, AssessmentRequest request);

    void deleteAssessmentById(UUID classId, UUID assessmentId);

    void persistAssessmentResource(UUID classId, UUID assessmentId, ResourceKind kind, List<ResourceRequest> requests);

    List<ResourceResponse> getAssessmentResources(UUID classId, UUID assessmentId);

    AssessmentResponse scheduleAssessment(UUID classId, UUID assessmentId, AssessmentScheduleRequest request);

    AssessmentResponse publishAssessment(UUID classId, UUID assessmentId, LocalDateTime dueDate);

    PagedResponse<List<AssessmentResponseForGrading>> getAllAssessments(Integer page, Integer size, AssessmentProperty property, Sort.Direction direction);

    AssessmentSummary getAssessmentsSummary(Month month);
}
