package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AnnotationRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AnnotationResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.Annotation;
import kr.com.mfa.mfaphase1api.model.entity.Answer;
import kr.com.mfa.mfaphase1api.model.enums.AnnotationProperty;
import kr.com.mfa.mfaphase1api.repository.AnnotationRepository;
import kr.com.mfa.mfaphase1api.repository.AnswerRepository;
import kr.com.mfa.mfaphase1api.service.AnnotationService;
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
public class AnnotationServiceImpl implements AnnotationService {

    private final AnnotationRepository annotationRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    @Override
    public AnnotationResponse createAnnotation(UUID answerId, AnnotationRequest request) {

        UUID currentUserId = extractCurrentUserId();

        Answer answer = answerRepository.findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Annotation annotation = Annotation.builder()
                .contentJson(request.getContentJson())
                .answer(answer)
                .build();

        return annotationRepository.saveAndFlush(annotation).toResponse();
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<AnnotationResponse>> getAllAnnotations(UUID answerId, Integer page, Integer size, AnnotationProperty property, Sort.Direction direction) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        answerRepository.findById(answerId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Annotation> pageAnnotations = switch (currentRole) {
            case "ROLE_ADMIN" -> annotationRepository
                    .findAllByAnswer_AnswerId(answerId, pageable);
            case "ROLE_INSTRUCTOR" -> annotationRepository
                    .findAllByAnswer_AnswerId_AndAnswer_Question_Assessment_CreatedBy(answerId, currentUserId, pageable);
            case "ROLE_STUDENT" -> annotationRepository
                    .findAllByAnswer_AnswerId_AndAnswer_Submission_StudentId(
                            answerId, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        List<AnnotationResponse> items = pageAnnotations.stream()
                .map(Annotation::toResponse)
                .toList();

        return pageResponse(
                items,
                pageAnnotations.getTotalElements(),
                page,
                size,
                pageAnnotations.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public AnnotationResponse getAnnotationById(UUID answerId, UUID annotationId) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        answerRepository.findById(answerId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Annotation annotation = switch (currentRole) {
            case "ROLE_ADMIN" ->
                    annotationRepository.findAnnotationByAnswer_AnswerId_AndAnnotationId(answerId, annotationId).orElseThrow(
                            () -> new NotFoundException("Annotation with ID " + annotationId + " not found")
                    );
            case "ROLE_INSTRUCTOR" ->
                    annotationRepository.findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Question_Assessment_CreatedBy(answerId, annotationId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Annotation with ID " + annotationId + " not found")
                    );
            case "ROLE_STUDENT" ->
                    annotationRepository.findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Submission_StudentId(answerId, annotationId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Annotation with ID " + annotationId + " not found")
                    );
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        return annotation.toResponse();
    }

    @Transactional
    @Override
    public AnnotationResponse updateAnnotation(UUID answerId, UUID annotationId, AnnotationRequest request) {

        UUID currentUserId = extractCurrentUserId();

        Answer answer = answerRepository.findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Annotation annotation = annotationRepository.findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Question_Assessment_CreatedBy(answerId, annotationId, currentUserId).orElseThrow(
                () -> new NotFoundException("Annotation with ID " + annotationId + " not found")
        );

        annotation.setContentJson(request.getContentJson());
        annotation.setAnswer(answer);

        return annotation.toResponse();
    }

    @Transactional
    @Override
    public void deleteAnnotation(UUID answerId, UUID annotationId) {

        UUID currentUserId = extractCurrentUserId();

        answerRepository.findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Annotation annotation = annotationRepository.findAnnotationByAnswer_AnswerId_AndAnnotationId_AndAnswer_Question_Assessment_CreatedBy(answerId, annotationId, currentUserId).orElseThrow(
                () -> new NotFoundException("Annotation with ID " + annotationId + " not found")
        );

        annotationRepository.delete(annotation);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private String extractCurrentRole() {
        List<String> currentUserRole = Objects.requireNonNull(JwtUtils.getJwt()).getClaimAsStringList("roles");
        return currentUserRole.getFirst();
    }
}
