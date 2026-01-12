package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.MotivationCommentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationCommentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.enums.MotivationCommentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface MotivationService {

    MotivationContentResponse createMotivation(MotivationContentRequest request);

    PagedResponse<List<MotivationContentResponse>> getAllMotivations(Integer page, Integer size, MotivationContentProperty property, Sort.Direction direction, MotivationContentType type, UUID createdBy, Boolean isDefault, Boolean isBookmarked);

    MotivationContentResponse getMotivationById(UUID motivationContentId);

    MotivationContentResponse updateMotivation(UUID motivationContentId, MotivationContentRequest request);

    void deleteMotivation(UUID motivationContentId);

    void bookmarkMotivation(UUID motivationContentId);

    void removeBookmarkMotivation(UUID motivationContentId);

    MotivationCommentResponse commentMotivation(UUID motivationContentId, MotivationCommentRequest request);

    PagedResponse<List<MotivationCommentResponse>> getAllCommentsByMotivationContentId(UUID motivationContentId, Integer page, Integer size, MotivationCommentProperty property, Sort.Direction direction);

    MotivationCommentResponse updateMotivationComment(UUID motivationContentId, UUID commentId, MotivationCommentRequest request);

    void deleteMotivationComment(UUID motivationContentId, UUID commentId);
}
