package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationCommentRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.*;
import kr.com.mfa.mfaphase1api.model.entity.MotivationBookmark;
import kr.com.mfa.mfaphase1api.model.entity.MotivationComment;
import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
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

        return saved.toResponse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<MotivationContentResponse>> getAllMotivations(
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

        List<MotivationContentResponse> items = contents.stream()
                .map(m -> m.toResponse(bookmarkedIds.contains(m.getMotivationContentId())))
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
    public MotivationContentResponse getMotivationById(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository
                .findById(motivationContentId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Motivation content with ID " + motivationContentId + " not found"
                        )
                );

        boolean isBookmarked = motivationBookmarkRepository
                .findByStudentIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        return motivationContent.toResponse(isBookmarked);
    }


    @Transactional
    @Override
    public MotivationContentResponse updateMotivation(UUID motivationContentId, MotivationContentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        motivationContentRepository.findByCreatedByAndMotivationContentId(currentUserId, motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationContent saved = motivationContentRepository.saveAndFlush(request.toEntity(currentUserId, motivationContentId));

        return saved.toResponse(null);
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

        boolean isBookmarked = motivationBookmarkRepository.findByStudentIdAndMotivationContent(currentUserId, motivationContent).isPresent();

        if (isBookmarked) {
            throw new ConflictException("You have already bookmarked this motivation content");
        }

        MotivationBookmark newMotivationBookmark = MotivationBookmark.builder()
                .motivationContent(motivationContent)
                .studentId(currentUserId)
                .build();

        motivationBookmarkRepository.save(newMotivationBookmark);
    }

    @Transactional
    @Override
    public void removeBookmarkMotivation(UUID motivationContentId) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationBookmark motivationBookmark = motivationBookmarkRepository.findByStudentIdAndMotivationContent(currentUserId, motivationContent).orElseThrow(
                () -> new BadRequestException("You have not bookmarked this motivation content")
        );

        motivationBookmarkRepository.delete(motivationBookmark);
    }

    @Transactional
    @Override
    public MotivationCommentResponse commentMotivation(UUID motivationContentId, MotivationCommentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        UserResponse userResponse = Objects.requireNonNull(userClient.getUserInfoById(currentUserId).getBody()).getPayload();

        StudentResponse studentResponse = StudentResponse.builder()
                .studentId(userResponse.getUserId())
                .studentEmail(userResponse.getEmail())
                .studentName(buildFullName(userResponse))
                .profileImage(userResponse.getProfileImage())
                .build();

        MotivationContent motivationContent = motivationContentRepository.findById(motivationContentId).orElseThrow(
                () -> new NotFoundException("Motivation content with ID " + motivationContentId + " not found")
        );

        MotivationComment motivationComment = MotivationComment.builder()
                .comment(request.getComment())
                .studentId(currentUserId)
                .motivationContent(motivationContent)
                .build();

        MotivationComment saved = motivationCommentRepository.saveAndFlush(motivationComment);

        return saved.toResponse(studentResponse);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private String buildFullName(UserResponse userResponse) {
        String firstName = userResponse.getFirstName() != null ? userResponse.getFirstName() : "";
        String lastName = userResponse.getLastName() != null ? userResponse.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }
}

