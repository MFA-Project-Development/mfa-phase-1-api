package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationCommentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.ReplyRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.MotivationBookmark;
import kr.com.mfa.mfaphase1api.model.entity.MotivationComment;
import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.entity.MotivationLike;
import kr.com.mfa.mfaphase1api.model.enums.MotivationCommentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.MotivationService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotivationServiceImpl implements MotivationService {

    private final MotivationContentRepository motivationContentRepository;
    private final MotivationSessionRepository motivationSessionRepository;
    private final MotivationStudentLogRepository motivationStudentLogRepository;
    private final MotivationBookmarkRepository motivationBookmarkRepository;
    private final MotivationCommentRepository motivationCommentRepository;
    private final MotivationLikeRepository motivationLikeRepository;
    private final UserClient userClient;

    @Transactional
    @Override
    public MotivationContentResponse createMotivation(MotivationContentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent saved = motivationContentRepository.saveAndFlush(request.toEntity(currentUserId));

        return saved.toResponse();
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<MotivationContentResponseWithExtraInfo>> getAllMotivations(
            Integer page,
            Integer size,
            MotivationContentProperty property,
            Sort.Direction direction,
            MotivationContentType type,
            UUID createdBy,
            Boolean isDefault,
            Boolean isBookmarked
    ) {
        UUID currentUserId = extractCurrentUserId();

        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 10 : size;

        String sortProp = (property == null || property.getProperty() == null || property.getProperty().isBlank())
                ? "createdAt"
                : property.getProperty();

        Sort.Direction sortDir = (direction == null) ? Sort.Direction.DESC : direction;
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, Sort.by(sortDir, sortProp));

        Page<MotivationContent> contentPage;
        if (Boolean.TRUE.equals(isBookmarked)) {
            contentPage = motivationContentRepository.searchBookmarked(
                    currentUserId, type, createdBy, isDefault, pageable
            );
        } else if (Boolean.FALSE.equals(isBookmarked)) {
            contentPage = motivationContentRepository.searchNotBookmarked(
                    currentUserId, type, createdBy, isDefault, pageable
            );
        } else {
            contentPage = motivationContentRepository.search(
                    type, createdBy, isDefault, pageable
            );
        }

        List<MotivationContent> contents = contentPage.getContent();

        Set<UUID> bookmarkedIds;
        if (Boolean.TRUE.equals(isBookmarked)) {
            bookmarkedIds = contents.stream()
                    .map(MotivationContent::getMotivationContentId)
                    .collect(Collectors.toSet());
        } else {
            bookmarkedIds = new HashSet<>(
                    motivationBookmarkRepository.findBookmarkedContentIds(currentUserId, contents)
            );
        }

        Set<UUID> likedIds = new HashSet<>(
                motivationLikeRepository.findLikedContentIds(currentUserId, contents)
        );

        List<MotivationContentResponseWithExtraInfo> items = contents.stream()
                .map(m -> m.toResponse(
                        bookmarkedIds.contains(m.getMotivationContentId()),
                        likedIds.contains(m.getMotivationContentId())
                ))
                .toList();

        return pageResponse(
                items,
                contentPage.getTotalElements(),
                safePage,
                safeSize,
                contentPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public MotivationContentResponseWithExtraInfo getMotivationById(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository
                .findById(motivationContentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Motivation content with ID " + motivationContentId + " not found"
                        )
                );

        boolean isBookmarked = motivationBookmarkRepository
                .findByUserIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        boolean isLiked = motivationLikeRepository
                .findByUserIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        return motivationContent.toResponse(isBookmarked, isLiked);
    }


    @Transactional
    @Override
    public MotivationContentResponse updateMotivation(UUID motivationContentId, MotivationContentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        motivationContentRepository.findByCreatedByAndMotivationContentId(currentUserId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationContent saved = motivationContentRepository.saveAndFlush(request.toEntity(currentUserId, motivationContentId));

        return saved.toResponse();
    }

    @Transactional
    @Override
    public void deleteMotivation(UUID motivationContentId) {
        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findByCreatedByAndMotivationContentId(currentUserId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        motivationContentRepository.delete(motivationContent);
    }

    @Transactional
    @Override
    public void bookmarkMotivation(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        boolean isBookmarked = motivationBookmarkRepository.findByUserIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        if (isBookmarked) {
            throw new ConflictException("You have already bookmarked this motivation content");
        }

        MotivationBookmark newMotivationBookmark = MotivationBookmark.builder()
                .motivationContent(motivationContent)
                .userId(currentUserId)
                .build();

        motivationBookmarkRepository.saveAndFlush(newMotivationBookmark);
    }

    @Transactional
    @Override
    public void removeBookmarkMotivation(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationBookmark motivationBookmark = motivationBookmarkRepository.findByUserIdAndMotivationContent(currentUserId, motivationContent).orElseThrow(
                () -> new NotFoundException("You have not bookmarked this motivation content")
        );

        motivationBookmarkRepository.delete(motivationBookmark);
    }

    @Transactional
    @Override
    public MotivationCommentResponse commentMotivation(UUID motivationContentId, MotivationCommentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        UserResponse userResponse = Objects.requireNonNull(userClient.getUserInfoById(currentUserId).getBody()).getPayload();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationComment motivationComment = MotivationComment.builder()
                .comment(request.getComment())
                .userId(currentUserId)
                .motivationContent(motivationContent)
                .build();

        MotivationComment saved = motivationCommentRepository.saveAndFlush(motivationComment);

        return saved.toResponse(userResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<MotivationCommentResponseWithReply>> getAllCommentsByMotivationContentId(
            UUID motivationContentId,
            Integer page,
            Integer size,
            MotivationCommentProperty property,
            Sort.Direction direction
    ) {
        motivationContentRepository.findById(motivationContentId)
                .orElseThrow(() -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found"));

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<MotivationComment> rootPage =
                motivationCommentRepository.findAllByMotivationContent_MotivationContentIdAndParentIsNull(
                        motivationContentId, pageable
                );

        List<MotivationComment> all =
                motivationCommentRepository.findAllByMotivationContent_MotivationContentId(motivationContentId);

        Map<UUID, List<MotivationComment>> repliesMap = new HashMap<>();
        for (MotivationComment c : all) {
            if (c.getParent() != null) {
                UUID parentId = c.getParent().getMotivationCommentId();
                repliesMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
            }
        }

        Map<UUID, UserResponse> userCache = new HashMap<>();

        List<MotivationCommentResponseWithReply> items = rootPage.getContent().stream()
                .map(root -> buildResponse(root, repliesMap, userCache))
                .toList();

        return pageResponse(
                items,
                rootPage.getTotalElements(),
                page,
                size,
                rootPage.getTotalPages()
        );
    }

    @Transactional
    @Override
    public MotivationCommentResponse updateMotivationComment(UUID motivationContentId, UUID commentId, MotivationCommentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        UserResponse userResponse = Objects.requireNonNull(userClient.getUserInfoById(currentUserId).getBody()).getPayload();

        MotivationComment motivationComment = motivationCommentRepository.findByMotivationCommentId_AndUserId_AndMotivationContent_MotivationContentId(commentId, currentUserId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation comment with ID " + commentId + " not found")
        );

        motivationComment.setComment(request.getComment());
        MotivationComment saved = motivationCommentRepository.saveAndFlush(motivationComment);

        return saved.toResponse(userResponse);
    }

    @Transactional
    @Override
    public void deleteMotivationComment(UUID motivationContentId, UUID commentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationComment motivationComment = motivationCommentRepository.findByMotivationCommentId_AndUserId_AndMotivationContent_MotivationContentId(commentId, currentUserId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation comment with ID " + commentId + " not found")
        );

        motivationCommentRepository.delete(motivationComment);
    }

    @Transactional
    @Override
    public MotivationCommentResponse replyCommentMotivation(UUID motivationContentId, ReplyRequest request) {

        UUID currentUserId = extractCurrentUserId();
        UUID commentId = request.getParentId();

        UserResponse userResponse = Objects.requireNonNull(userClient.getUserInfoById(currentUserId).getBody()).getPayload();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationComment motivationComment = motivationCommentRepository.findByMotivationCommentId_AndMotivationContent_MotivationContentId(commentId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation comment with ID " + commentId + " not found")
        );

        MotivationComment newMotivationComment = MotivationComment.builder()
                .comment(request.getComment())
                .userId(currentUserId)
                .motivationContent(motivationContent)
                .parent(motivationComment)
                .build();

        MotivationComment saved = motivationCommentRepository.saveAndFlush(newMotivationComment);

        return saved.toResponse(userResponse);
    }

    @Override
    public void likeMotivation(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        boolean isLiked = motivationLikeRepository.findByUserIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        if (isLiked) {
            throw new ConflictException("You have already liked this motivation content");
        }

        MotivationLike motivationLike = MotivationLike.builder()
                .motivationContent(motivationContent)
                .userId(currentUserId)
                .build();

        motivationLikeRepository.saveAndFlush(motivationLike);
    }

    @Override
    public void unlikeMotivation(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationLike motivationLike = motivationLikeRepository.findByUserIdAndMotivationContent(currentUserId, motivationContent).orElseThrow(
                () -> new NotFoundException("You have not bookmarked this motivation content")
        );

        motivationLikeRepository.delete(motivationLike);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private MotivationCommentResponseWithReply buildResponse(
            MotivationComment comment,
            Map<UUID, List<MotivationComment>> repliesMap,
            Map<UUID, UserResponse> userCache
    ) {
        UserResponse user = userCache.computeIfAbsent(comment.getUserId(), userId ->
                Objects.requireNonNull(
                        userClient.getUserInfoById(userId).getBody()
                ).getPayload()
        );

        List<MotivationCommentResponseWithReply> replies = repliesMap
                .getOrDefault(comment.getMotivationCommentId(), List.of())
                .stream()
                .map(r -> buildResponse(r, repliesMap, userCache))
                .toList();

        return comment.toResponse(user, replies);
    }

}

