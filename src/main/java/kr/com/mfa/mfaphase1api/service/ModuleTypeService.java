package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.ModuleTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.ModuleTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.ModuleTypeProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface ModuleTypeService {
    ModuleTypeResponse createModuleType(ModuleTypeRequest request);

    PagedResponse<List<ModuleTypeResponse>> getAllModuleTypes( Integer page, Integer size, ModuleTypeProperty property, Sort.Direction direction);

    ModuleTypeResponse getModuleTypeById(UUID moduleTypeId);

    ModuleTypeResponse updateModuleTypeById(UUID moduleTypeId, ModuleTypeRequest request);

    void deleteModuleTypeById(UUID moduleTypeId);
}
