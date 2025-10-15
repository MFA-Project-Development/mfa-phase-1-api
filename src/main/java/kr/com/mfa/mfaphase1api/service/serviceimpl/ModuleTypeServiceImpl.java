package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.model.dto.request.ModuleTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.ModuleTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.ModuleType;
import kr.com.mfa.mfaphase1api.model.enums.ModuleTypeProperty;
import kr.com.mfa.mfaphase1api.repository.ModuleTypeRepository;
import kr.com.mfa.mfaphase1api.service.ModuleTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
public class ModuleTypeServiceImpl implements ModuleTypeService {

    private final ModuleTypeRepository moduleTypeRepository;

    @Override
    @Transactional
    public ModuleTypeResponse createModuleType(ModuleTypeRequest request) {

        assertNameUnique(request.getType());
        ModuleType saved = moduleTypeRepository.save(request.toEntity());

        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<ModuleTypeResponse>> getAllModuleTypes(Integer page, Integer size, ModuleTypeProperty property, Sort.Direction direction) {
        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<ModuleType> pageModuleTypes = moduleTypeRepository.findAll(pageable);

        List<ModuleTypeResponse> items = pageModuleTypes
                .getContent()
                .stream()
                .map(ModuleType::toResponse)
                .toList();

        return pageResponse(
                items,
                pageModuleTypes.getTotalElements(),
                page,
                size,
                pageModuleTypes.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleTypeResponse getModuleTypeById(UUID moduleTypeId) {
        ModuleType moduleType = getOrThrow(moduleTypeId);
        return moduleType.toResponse();
    }

    @Override
    @Transactional
    public ModuleTypeResponse updateModuleTypeById(UUID moduleTypeId, ModuleTypeRequest request) {

        getOrThrow(moduleTypeId);
        assertNameUnique(request.getType());
        ModuleType saved = moduleTypeRepository.save(request.toEntity(moduleTypeId));

        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteModuleTypeById(UUID moduleTypeId) {
        getOrThrow(moduleTypeId);
        moduleTypeRepository.deleteById(moduleTypeId);
    }

    private ModuleType getOrThrow(UUID moduleTypeId) {
        return moduleTypeRepository
                .findById(moduleTypeId)
                .orElseThrow(() -> new ConflictException("ModuleType " + moduleTypeId + "not found"));
    }

    private void assertNameUnique(String type) {
        boolean exists =  moduleTypeRepository.existsByTypeIgnoreCase(type);

        if (exists) {
            throw new ConflictException("ModuleType already exists");
        }
    }
}
