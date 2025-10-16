package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.QuestionTypeRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionTypeResponse;
import kr.com.mfa.mfaphase1api.model.enums.QuestionTypeProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface QuestionTypeService {
    QuestionTypeResponse createQuestionType(QuestionTypeRequest request);

    PagedResponse<List<QuestionTypeResponse>> getAllQuestionTypes( Integer page,  Integer size, QuestionTypeProperty property, Sort.Direction direction);

    QuestionTypeResponse getQuestionTypeById(UUID questionTypeId);

    QuestionTypeResponse updateQuestionTypeById(UUID questionTypeId, QuestionTypeRequest request);

    void deleteQuestionTypeById(UUID questionTypeId);   
}
