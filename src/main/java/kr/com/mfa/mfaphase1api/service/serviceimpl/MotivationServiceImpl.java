package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.model.dto.request.MotivationContentRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.MotivationContent;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentProperty;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import kr.com.mfa.mfaphase1api.repository.MotivationContentRepository;
import kr.com.mfa.mfaphase1api.repository.MotivationSessionRepository;
import kr.com.mfa.mfaphase1api.repository.MotivationStudentLogRepository;
import kr.com.mfa.mfaphase1api.service.MotivationService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
public class MotivationServiceImpl implements MotivationService {

    private final MotivationContentRepository motivationContentRepository;
    private final MotivationSessionRepository motivationSessionRepository;
    private final MotivationStudentLogRepository motivationStudentLogRepository;

    @Override
    @Transactional
    public MotivationContentResponse createMotivation(MotivationContentRequest request) {

        UUID currentUserId = extractCurrentUserId();

        MotivationContent saved = motivationContentRepository.saveAndFlush(request.toEntity(currentUserId));

        return saved.toResponse();
    }

    @Override
    public PagedResponse<List<MotivationContentResponse>> getAllMotivations(Integer page, Integer size, MotivationContentProperty property, Sort.Direction direction, MotivationContentType type, UUID createdBy, Boolean isDefault) {

        UUID currentUserId = extractCurrentUserId();

        String currentUserRole = extractCurrentRole();

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<MotivationContent> pageMotivationContents = switch (currentUserRole) {
            case "ROLE_ADMIN" ->
                    motivationContentRepository.findAllByTypeOrCreatedByOrIsDefault(type, createdBy, isDefault, pageable);
            case "ROLE_INSTRUCTOR" ->
                    motivationContentRepository.findAllByTypeOrCreatedByOrIsDefault(type, currentUserId, isDefault, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole);
        };

        List<MotivationContentResponse> items = pageMotivationContents.stream()
                .map(MotivationContent::toResponse)
                .toList();

        return pageResponse(
                items,
                pageMotivationContents.getTotalElements(),
                page,
                size,
                pageMotivationContents.getTotalPages()
        );
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private String extractCurrentRole() {
        List<String> currentUserRole = Objects.requireNonNull(JwtUtils.getJwt()).getClaimAsStringList("roles");
        return currentUserRole.getFirst();
    }

}

