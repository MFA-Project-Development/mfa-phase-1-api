package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionTypeResponse;
import kr.com.mfa.mfaphase1api.model.entity.QuestionType;
import kr.com.mfa.mfaphase1api.model.enums.QuestionTypeProperty;
import kr.com.mfa.mfaphase1api.repository.QuestionTypeRepository;
import kr.com.mfa.mfaphase1api.service.QuestionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
public class QuestionTypeServiceImpl implements QuestionTypeService {

    private final QuestionTypeRepository questionTypeRepository;

    @Override
    @Transactional
    public QuestionTypeResponse createQuestionType(QuestionTypeRequest request) {
        assertNameUnique(request.getType());
        QuestionType saved = questionTypeRepository.save(request.toEntity());
        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<QuestionTypeResponse>> getAllQuestionTypes(
            Integer page,
            Integer size,
            QuestionTypeProperty property,
            Sort.Direction direction
    ) {
        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<QuestionType> pageQuestionTypes = questionTypeRepository.findAll(pageable);

        List<QuestionTypeResponse> items = pageQuestionTypes
                .getContent()
                .stream()
                .map(QuestionType::toResponse)
                .toList();

        return pageResponse(
                items,
                pageQuestionTypes.getTotalElements(),
                page,
                size,
                pageQuestionTypes.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionTypeResponse getQuestionTypeById(UUID questionTypeId) {
        QuestionType qt = getOrThrow(questionTypeId);
        return qt.toResponse();
    }

    @Override
    @Transactional
    public QuestionTypeResponse updateQuestionTypeById(UUID questionTypeId, QuestionTypeRequest request) {
        getOrThrow(questionTypeId);
        assertNameUnique(request.getType());
        QuestionType saved = questionTypeRepository.save(request.toEntity(questionTypeId));
        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteQuestionTypeById(UUID questionTypeId) {
        getOrThrow(questionTypeId);
        questionTypeRepository.deleteById(questionTypeId);
    }

    private QuestionType getOrThrow(UUID questionTypeId) {
        return questionTypeRepository.findById(questionTypeId)
                .orElseThrow(() ->
                        new NotFoundException("QuestionType with ID " + questionTypeId + " not found"));
    }

    private void assertNameUnique(String type) {
        boolean exists = questionTypeRepository.existsByTypeIgnoreCase(type);
        if (exists) {
            throw new ConflictException("QuestionType with type '" + type + "' already exists");
        }
    }
}
