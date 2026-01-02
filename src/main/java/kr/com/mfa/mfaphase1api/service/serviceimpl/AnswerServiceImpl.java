package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.AnswerRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.AnswerResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.AnswerProperty;
import kr.com.mfa.mfaphase1api.repository.*;
import kr.com.mfa.mfaphase1api.service.AnswerService;
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
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final PaperRepository paperRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    @Override
    public AnswerResponse gradeAnswer(UUID questionId, AnswerRequest request) {

        UUID currentUserId = extractCurrentUserId();

        UUID paperId = request.getPaperId();
        UUID submissionId = request.getSubmissionId();

        Question question = questionRepository.findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId).orElseThrow(
                () -> new NotFoundException("Question not " + questionId + " found")
        );

        Submission submission = submissionRepository.findBySubmissionId_AndAssessment_AssessmentId(submissionId, question.getAssessment().getAssessmentId()).orElseThrow(
                () -> new NotFoundException("Submission not " + submissionId + " found")
        );

        Paper paper = paperRepository.findBySubmission_SubmissionIdAndPaperId(submission.getSubmissionId(), paperId).orElseThrow(
                () -> new NotFoundException("Paper not " + paperId + " found")
        );

        Answer newAnswer = Answer.builder()
                .pointsAwarded(request.getPointsAwarded())
                .paper(paper)
                .question(question)
                .submission(submission)
                .build();

        return answerRepository.saveAndFlush(newAnswer).toResponse();
    }

    @Transactional(readOnly = true)
    @Override
    public AnswerResponse getAnswerById(UUID questionId, UUID answerId) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        questionRepository.findById(questionId).orElseThrow(
                () -> new NotFoundException("Question not " + questionId + " found")
        );

        Answer answer = switch (currentRole) {
            case "ROLE_ADMIN" ->
                    answerRepository.findAnswersByQuestion_QuestionId_AndAnswerId(questionId, answerId).orElseThrow(
                            () -> new NotFoundException("Answer with ID " + answerId + " not found")
                    );
            case "ROLE_INSTRUCTOR" ->
                    answerRepository.findAnswersByQuestion_QuestionId_AndAnswerId_AndQuestion_Assessment_CreatedBy(questionId, answerId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Answer with ID " + answerId + " not found")
                    );
            case "ROLE_STUDENT" ->
                    answerRepository.findAnswersByQuestion_QuestionId_AndAnswerId_AndSubmission_StudentId(questionId, answerId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Answer with ID " + answerId + " not found")
                    );
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        return answer.toResponse();
    }

    @Transactional
    @Override
    public AnswerResponse updateAnswer(UUID questionId, UUID answerId, AnswerRequest request) {

        UUID currentUserId = extractCurrentUserId();
        UUID submissionId = request.getSubmissionId();
        UUID paperId = request.getPaperId();

        Question question = questionRepository.findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId).orElseThrow(
                () -> new NotFoundException("Question not " + questionId + " found")
        );

        Submission submission = submissionRepository.findBySubmissionId_AndAssessment_AssessmentId(submissionId, question.getAssessment().getAssessmentId()).orElseThrow(
                () -> new NotFoundException("Submission not " + submissionId + " found")
        );

        Paper paper = paperRepository.findBySubmission_SubmissionIdAndPaperId(submission.getSubmissionId(), paperId).orElseThrow(
                () -> new NotFoundException("Paper not " + paperId + " found")
        );

        Answer answer = answerRepository.findAnswersByQuestion_QuestionId_AndAnswerId_AndQuestion_Assessment_CreatedBy(questionId, answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        answer.setPointsAwarded(request.getPointsAwarded());
        answer.setPaper(paper);
        answer.setSubmission(submission);
        answer.setQuestion(question);

        return answer.toResponse();
    }

    @Transactional
    @Override
    public void deleteAnswer(UUID questionId, UUID answerId) {

        UUID currentUserId = extractCurrentUserId();

        questionRepository.findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId).orElseThrow(
                () -> new NotFoundException("Question not " + questionId + " found")
        );

        answerRepository.findAnswersByQuestion_QuestionId_AndAnswerId_AndQuestion_Assessment_CreatedBy(questionId, answerId, currentUserId).orElseThrow(
                () -> new NotFoundException("Answer with ID " + answerId + " not found")
        );

        answerRepository.deleteById(answerId);

    }

    @Transactional(readOnly = true)
    @Override
    public PagedResponse<List<AnswerResponse>> getAllAnswers(UUID questionId, Integer page, Integer size, AnswerProperty property, Sort.Direction direction) {

        UUID currentUserId = extractCurrentUserId();
        String currentRole = extractCurrentRole();

        questionRepository.findById(questionId).orElseThrow(
                () -> new NotFoundException("Question not " + questionId + " found")
        );

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<Answer> pageAnswers = switch (currentRole) {
            case "ROLE_ADMIN" -> answerRepository
                    .findAllByQuestion_QuestionId(questionId, pageable);
            case "ROLE_INSTRUCTOR" -> answerRepository
                    .findAllByQuestion_QuestionId_AndQuestion_Assessment_CreatedBy(questionId, currentUserId, pageable);
            case "ROLE_STUDENT" -> answerRepository
                    .findAllByQuestion_QuestionId_AndSubmission_StudentId(
                            questionId, currentUserId, pageable);
            default -> throw new ForbiddenException("Unsupported role: " + currentRole);
        };

        List<AnswerResponse> items = pageAnswers.stream()
                .map(Answer::toResponse)
                .toList();

        return pageResponse(
                items,
                pageAnswers.getTotalElements(),
                page,
                size,
                pageAnswers.getTotalPages()
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
