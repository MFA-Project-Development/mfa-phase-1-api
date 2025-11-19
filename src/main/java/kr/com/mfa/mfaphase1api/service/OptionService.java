package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.Valid;
import kr.com.mfa.mfaphase1api.model.dto.request.OptionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.OptionResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.OptionProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface OptionService {
    OptionResponse createOption(UUID questionId, OptionRequest request);

    PagedResponse<List<OptionResponse>> getAllOptions(UUID questionId, Integer page, Integer size, OptionProperty property, Sort.Direction direction);

    OptionResponse getOptionById(UUID questionId, UUID optionId);

    OptionResponse updateOptionById(UUID questionId, UUID optionId, OptionRequest request);

    void deleteOptionById(UUID questionId, UUID optionId);

    List<OptionResponse> createMultipleOptions(UUID questionId, List<OptionRequest> requests);
}
