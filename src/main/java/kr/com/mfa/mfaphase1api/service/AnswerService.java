package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import kr.com.mfa.mfaphase1api.model.dto.request.AnswerRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AnswerResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.AnswerProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface AnswerService {

    AnswerResponse gradeAnswer(UUID questionId, AnswerRequest request);

    AnswerResponse getAnswerById(UUID questionId, UUID answerId);

    AnswerResponse updateAnswer(UUID questionId, UUID answerId, AnswerRequest request);

    void deleteAnswer(UUID questionId, UUID answerId);

    PagedResponse<List<AnswerResponse>> getAllAnswers(UUID questionId, Integer page, Integer size, AnswerProperty property, Sort.Direction direction);

    PagedResponse<List<AnswerResponse>> getAllAnswersBySubmissionId(UUID submissionId,Integer page,Integer size, AnswerProperty property, Sort.Direction direction);
}
