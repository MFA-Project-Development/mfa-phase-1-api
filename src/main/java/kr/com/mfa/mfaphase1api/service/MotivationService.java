package kr.com.mfa.mfaphase1api.service;

import jakarta.validation.Valid;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationCommentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ReplyRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.enums.MotivationCommentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface MotivationService {

    MotivationContentResponse createMotivation(MotivationContentRequest request);

    PagedResponse<List<MotivationContentResponseWithExtraInfo>> getAllMotivations(Integer page, Integer size, MotivationContentProperty property, Sort.Direction direction, MotivationContentType type, UUID createdBy, Boolean isDefault, Boolean isBookmarked);

    MotivationContentResponseWithExtraInfo getMotivationById(UUID motivationContentId);

    MotivationContentResponse updateMotivation(UUID motivationContentId, MotivationContentRequest request);

    void deleteMotivation(UUID motivationContentId);

    void bookmarkMotivation(UUID motivationContentId);

    void removeBookmarkMotivation(UUID motivationContentId);

    MotivationCommentResponse commentMotivation(UUID motivationContentId, MotivationCommentRequest request);

    PagedResponse<List<MotivationCommentResponseWithReply>> getAllCommentsByMotivationContentId(UUID motivationContentId, Integer page, Integer size, MotivationCommentProperty property, Sort.Direction direction);

    MotivationCommentResponse updateMotivationComment(UUID motivationContentId, UUID commentId, MotivationCommentRequest request);

    void deleteMotivationComment(UUID motivationContentId, UUID commentId);

    MotivationCommentResponse replyCommentMotivation(UUID motivationContentId, ReplyRequest request);

    void likeMotivation(UUID motivationContentId);

    void unlikeMotivation(UUID motivationContentId);
}
