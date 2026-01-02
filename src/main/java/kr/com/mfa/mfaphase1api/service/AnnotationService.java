package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.AnnotationRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AnnotationResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AnnotationProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface AnnotationService {
    AnnotationResponse createAnnotation(UUID answerId, AnnotationRequest request);

    PagedResponse<List<AnnotationResponse>> getAllAnnotations(UUID answerId,  Integer page,  Integer size, AnnotationProperty property, Sort.Direction direction);

    AnnotationResponse getAnnotationById(UUID answerId, UUID annotationId);

    AnnotationResponse updateAnnotation(UUID answerId, UUID annotationId, AnnotationRequest request);

    void deleteAnnotation(UUID answerId, UUID annotationId);
}
