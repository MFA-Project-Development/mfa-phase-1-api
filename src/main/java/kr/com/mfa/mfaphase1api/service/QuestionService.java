package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.Valid;
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.enums.QuestionProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
    QuestionResponse createQuestion(UUID assessmentId, QuestionRequest request);

    PagedResponse<List<QuestionResponse>> getAllQuestions(UUID assessmentId, Integer page, Integer size, QuestionProperty property, Sort.Direction direction);

    QuestionResponse getQuestionById(UUID assessmentId, UUID questionId);

    QuestionResponse updateQuestionById(UUID assessmentId, UUID questionId, QuestionRequest request);

    void deleteQuestionById(UUID assessmentId, UUID questionId);

    List<QuestionResponse> createMultipleQuestions(UUID assessmentId, List<QuestionRequest> requests);
}
