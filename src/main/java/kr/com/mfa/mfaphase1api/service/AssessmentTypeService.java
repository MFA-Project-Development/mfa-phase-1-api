package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentTypeProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface AssessmentTypeService {
    AssessmentTypeResponse createAssessmentType(AssessmentTypeRequest request);

    PagedResponse<List<AssessmentTypeResponse>> getAllAssessmentTypes(Integer page, Integer size, AssessmentTypeProperty property, Sort.Direction direction);

    AssessmentTypeResponse getAssessmentTypeById(UUID assessmentTypeId);

    AssessmentTypeResponse updateAssessmentTypeById(UUID assessmentTypeId, AssessmentTypeRequest request);

    void deleteAssessmentTypeById(UUID assessmentTypeId);
}
