package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.FeedbackRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.FeedbackResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.FeedbackProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface FeedbackService {
    FeedbackResponse createFeedback(UUID answerId, FeedbackRequest request);

    PagedResponse<List<FeedbackResponse>> getAllFeedbacks(UUID answerId, Integer page, Integer size, FeedbackProperty property, Sort.Direction direction);

    FeedbackResponse getFeedbackById(UUID answerId, UUID feedbackId);

    FeedbackResponse updateFeedback(UUID answerId, UUID feedbackId, FeedbackRequest request);

    void deleteFeedback(UUID answerId, UUID feedbackId);
}
