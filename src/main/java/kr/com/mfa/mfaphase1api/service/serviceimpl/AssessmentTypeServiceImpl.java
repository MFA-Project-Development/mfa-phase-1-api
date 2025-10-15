package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AssessmentTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentTypeResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.AssessmentType;
import kr.com.mfa.mfaphase1api.model.enums.AssessmentTypeProperty;
import kr.com.mfa.mfaphase1api.repository.AssessmentTypeRepository;
import kr.com.mfa.mfaphase1api.service.AssessmentTypeService;
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
public class AssessmentTypeServiceImpl implements AssessmentTypeService {

    private final AssessmentTypeRepository assessmentTypeRepository;

    @Override
    @Transactional
    public AssessmentTypeResponse createAssessmentType(AssessmentTypeRequest request) {

        assertTypeUnique(request.getType());
        AssessmentType saved = assessmentTypeRepository.save(request.toEntity());

        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<AssessmentTypeResponse>> getAllAssessmentTypes(Integer page, Integer size, AssessmentTypeProperty property, Sort.Direction direction) {
        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<AssessmentType> pageAssessmentTypes = assessmentTypeRepository.findAll(pageable);

        List<AssessmentTypeResponse> items = pageAssessmentTypes
                .getContent()
                .stream()
                .map(AssessmentType::toResponse)
                .toList();

        return pageResponse(
                items,
                pageAssessmentTypes.getTotalElements(),
                page,
                size,
                pageAssessmentTypes.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentTypeResponse getAssessmentTypeById(UUID assessmentTypeId) {
        AssessmentType assessmentType = getOrThrow(assessmentTypeId);
        return assessmentType.toResponse();
    }

    @Override
    @Transactional
    public AssessmentTypeResponse updateAssessmentTypeById(UUID assessmentTypeId, AssessmentTypeRequest request) {
        getOrThrow(assessmentTypeId);
        assertTypeUnique(request.getType());
        AssessmentType saved = assessmentTypeRepository.save(request.toEntity(assessmentTypeId));
        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteAssessmentTypeById(UUID assessmentTypeId) {
        getOrThrow(assessmentTypeId);
        assessmentTypeRepository.deleteById(assessmentTypeId);
    }

    private AssessmentType getOrThrow(UUID assessmentTypeId) {
        return assessmentTypeRepository
                .findById(assessmentTypeId)
                .orElseThrow(() -> new NotFoundException("AssessmentType " + assessmentTypeId + "not found"));
    }

    private void assertTypeUnique(String type) {
        boolean exists = assessmentTypeRepository.existsByTypeIgnoreCase(type);

        if (exists) {
            throw new ConflictException("AssessmentType already exists");
        }
    }
}
