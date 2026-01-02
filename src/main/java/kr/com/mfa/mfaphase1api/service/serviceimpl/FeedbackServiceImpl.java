package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.FeedbackRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.FeedbackResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.Annotation;
import kr.com.mfa.mfaphase1api.model.entity.Answer;
import kr.com.mfa.mfaphase1api.model.entity.Feedback;
import kr.com.mfa.mfaphase1api.model.enums.FeedbackProperty;
import kr.com.mfa.mfaphase1api.repository.AnnotationRepository;
import kr.com.mfa.mfaphase1api.repository.AnswerRepository;
import kr.com.mfa.mfaphase1api.repository.FeedbackRepository;
import kr.com.mfa.mfaphase1api.service.FeedbackService;
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
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AnnotationRepository annotationRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    @Override
    public FeedbackResponse createFeedback(UUID answerId, FeedbackRequest request) {

        feedbackRepository.findFeedbackByAnswer_AnswerId_AndAnnotation_AnnotationId(answerId, request.getAnnotationId())
                .ifPresent(feedback -> {
                    throw new ConflictException("Feedback already exists for the given annotation");
                });

        UUID currentUserId = extractCurrentUserId();

        Annotation annotation = annotationRepository.findById(request.getAnnotationId())
                .orElseThrow(() -> new NotFoundException("Annotation with ID " + request.getAnnotationId() + " not found"));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer with ID " + answerId + " not found"));

        Feedback feedback = Feedback.builder()
                .comment(request.getComment())
                .answer(answer)
                .annotation(annotation)
                .authorId(currentUserId)
                .build();

        Feedback savedFeedback = feedbackRepository.saveAndFlush(feedback);

        return savedFeedback.toResponse();
    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<FeedbackResponse>> getAllFeedbacks(UUID answerId, Integer page, Integer size, FeedbackProperty property, Sort.Direction direction) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        answerRepository.findById(answerId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Feedback> pageFeedbacks = switch (currentRole) {
            case "ROLE_ADMIN" -> feedbackRepository
                    .findAllByAnswer_AnswerId(answerId, pageable);
            case "ROLE_INSTRUCTOR" -> feedbackRepository
                    .findAllByAnswer_AnswerId_AndAuthorId(answerId, currentUserId, pageable);
            case "ROLE_STUDENT" -> feedbackRepository
                    .findAllByAnswer_AnswerId_AndAnswer_Submission_StudentId(
                            answerId, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        List<FeedbackResponse> items = pageFeedbacks.stream()
                .map(Feedback::toResponse)
                .toList();

        return pageResponse(
                items,
                pageFeedbacks.getTotalElements(),
                page,
                size,
                pageFeedbacks.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public FeedbackResponse getFeedbackById(UUID answerId, UUID feedbackId) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        answerRepository.findById(answerId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Feedback feedback = switch (currentRole) {
            case "ROLE_ADMIN" ->
                    feedbackRepository.findFeedbackByAnswer_AnswerIdAndFeedbackId(answerId, feedbackId).orElseThrow(
                            () -> new NotFoundException("Feedback with ID " + feedbackId + " not found")
                    );
            case "ROLE_INSTRUCTOR" ->
                    feedbackRepository.findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAuthorId(answerId, feedbackId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Feedback with ID " + feedbackId + " not found")
                    );
            case "ROLE_STUDENT" ->
                    feedbackRepository.findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAnswer_Submission_StudentId(answerId, feedbackId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Feedback with ID " + feedbackId + " not found")
                    );
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        return feedback.toResponse();
    }

    @Transactional
    @Override
    public FeedbackResponse updateFeedback(UUID answerId, UUID feedbackId, FeedbackRequest request) {

        UUID currentUserId = extractCurrentUserId();

        Answer answer = answerRepository.findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        Feedback feedback = feedbackRepository.findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAuthorId(answerId, feedbackId, currentUserId).orElseThrow(
                () -> new NotFoundException("Feedback with ID " + feedbackId + " not found")
        );

        Annotation annotation = annotationRepository.findById(request.getAnnotationId())
                .orElseThrow(() -> new NotFoundException("Annotation with ID " + request.getAnnotationId() + " not found"));

        feedback.setComment(request.getComment());
        feedback.setAnswer(answer);
        feedback.setAnnotation(annotation);

        return feedback.toResponse();
    }

    @Transactional
    @Override
    public void deleteFeedback(UUID answerId, UUID feedbackId) {

        UUID currentUserId = extractCurrentUserId();

        answerRepository.findAnswerByAnswerId_AndQuestion_Assessment_CreatedBy(answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        feedbackRepository.findFeedbackByAnswer_AnswerIdAndFeedbackId_AndAuthorId(answerId, feedbackId, currentUserId).orElseThrow(
                () -> new NotFoundException("Feedback with ID " + feedbackId + " not found")
        );

        feedbackRepository.deleteById(feedbackId);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private String extractCurrentRole() {
        List<String> currentUserRole = Objects.requireNonNull(JwtUtils.getJwt()).getClaimAsStringList("roles");
        return currentUserRole.getFirst();
    }


}
