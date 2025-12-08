package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface MotivationService {

    MotivationContentResponse createMotivation(MotivationContentRequest request);

    PagedResponse<List<MotivationContentResponse>> getAllMotivations(Integer page, Integer size, MotivationContentProperty property, Sort.Direction direction, MotivationContentType type, UUID createdBy, Boolean isDefault);
}
