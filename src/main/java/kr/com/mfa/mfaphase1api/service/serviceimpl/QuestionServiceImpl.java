package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.QuestionProperty;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionTypeRepository;
import kr.com.mfa.mfaphase1api.service.QuestionService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionTypeRepository questionTypeRepository;
    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional
    public QuestionResponse createQuestion(UUID assessmentId, QuestionRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to create question in this assessment"));

        QuestionType questionType = questionTypeRepository.findById(request.getQuestionTypeId())
                .orElseThrow(() -> new NotFoundException("QuestionType not found"));

        int questionOrder = questionRepository.countByAssessment(assessment) + 1;

        Question saved = questionRepository.saveAndFlush(request.toEntity(questionOrder, questionType, assessment));

        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<QuestionResponse>> getAllQuestions(UUID assessmentId, Integer page, Integer size, QuestionProperty property, Sort.Direction direction) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Assessment assessment;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> assessment = assessmentRepository.findById(assessmentId).orElseThrow(
                    () -> new NotFoundException("Assessment not found")
            );

            case "ROLE_INSTRUCTOR" ->
                    assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Assessment not found"));

            case "ROLE_STUDENT" ->
                    assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Assessment not found")
                    );

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Question> pageQuestions = questionRepository.findAllByAssessment_AssessmentId(assessment.getAssessmentId(), pageable);

        List<QuestionResponse> items = pageQuestions.getContent().stream()
                .map(Question::toResponse)
                .toList();

        return pageResponse(
                items,
                pageQuestions.getTotalElements(),
                page,
                size,
                pageQuestions.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(UUID assessmentId, UUID questionId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Assessment assessment;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> assessment = assessmentRepository.findById(assessmentId).orElseThrow(
                    () -> new NotFoundException("Assessment " + assessmentId + " not found")
            );

            case "ROLE_INSTRUCTOR" ->
                    assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                            .orElseThrow(() -> new NotFoundException("Assessment " + assessmentId + " not found"));

            case "ROLE_STUDENT" ->
                    assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId).orElseThrow(
                            () -> new NotFoundException("Assessment " + assessmentId + " not found")
                    );

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        Question question = getOrThrow(assessment.getAssessmentId(), questionId);

        return question.toResponse();
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestionById(UUID assessmentId, UUID questionId, QuestionRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to modify question in this assessment"));

        Question question = getOrThrow(assessment.getAssessmentId(), questionId);

        QuestionType questionType = questionTypeRepository.findById(request.getQuestionTypeId())
                .orElseThrow(() -> new NotFoundException("QuestionType not found"));

        question.setText(request.getText());
        question.setPoints(request.getPoints());
        question.setMode(request.getMode());
        question.setQuestionType(questionType);

        return question.toResponse();
    }

    @Override
    @Transactional
    public void deleteQuestionById(UUID assessmentId, UUID questionId) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to delete question in this assessment"));

        Question question = getOrThrow(assessment.getAssessmentId(), questionId);
        questionRepository.delete(question);

    }

    private Question getOrThrow(UUID assessmentId, UUID questionId) {
        return questionRepository
                .findByAssessment_AssessmentId_AndQuestionId(assessmentId, questionId)
                .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));
    }
}
