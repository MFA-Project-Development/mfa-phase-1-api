package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.exception.UnauthorizeException;
import kr.com.mfa.mfaphase1api.model.dto.request.QuestionRequest;
import kr.com.mfa.mfaphase1api.model.dto.request.UpdateQuestionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.QuestionResponse;
import kr.com.mfa.mfaphase1api.model.entity.*;
import kr.com.mfa.mfaphase1api.model.enums.QuestionProperty;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionImageRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionRepository;
//import kr.com.mfa.mfaphase1api.repository.QuestionTypeRepository;
import kr.com.mfa.mfaphase1api.service.FileService;
import kr.com.mfa.mfaphase1api.service.QuestionService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    //    private final QuestionTypeRepository questionTypeRepository;
    private final AssessmentRepository assessmentRepository;
    private final QuestionImageRepository questionImageRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public QuestionResponse createQuestion(UUID assessmentId, QuestionRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to create question in this assessment"));

//        QuestionType questionType = questionTypeRepository.findById(request.getQuestionTypeId())
//                .orElseThrow(() -> new NotFoundException("QuestionType not found"));

        int questionOrder = questionRepository.countByAssessment(assessment) + 1;

        Question question = request.toEntity(questionOrder, assessment);

        if (request.getQuestionImages() != null) {
            int imageOrder = 1;
            for (String imageUrl : request.getQuestionImages()) {
                QuestionImage questionImage = QuestionImage.builder()
                        .imageOrder(imageOrder++)
                        .imageUrl(imageUrl)
                        .question(question)
                        .build();
                question.getQuestionImages().add(questionImage);
            }
        }

        Question saved = questionRepository.saveAndFlush(question);

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

//        QuestionType questionType = questionTypeRepository.findById(request.getQuestionTypeId())
//                .orElseThrow(() -> new NotFoundException("QuestionType not found"));

        question.setText(request.getText());
        question.setPoints(request.getPoints());
        question.setMode(request.getMode());
        question.setQuestionType(request.getQuestionType());

        question.getQuestionImages().removeAll(question.getQuestionImages());

        if (request.getQuestionImages() != null) {
            int imageOrder = 1;
            for (String imageUrl : request.getQuestionImages()) {
                QuestionImage questionImage = QuestionImage.builder()
                        .imageOrder(imageOrder++)
                        .imageUrl(imageUrl)
                        .question(question)
                        .build();
                question.getQuestionImages().add(questionImage);
            }
        }

        Question saved = questionRepository.saveAndFlush(question);

        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteQuestionById(UUID assessmentId, UUID questionId) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to delete question in this assessment"));

        Question question = getOrThrow(assessment.getAssessmentId(), questionId);

        if (question.getQuestionImages() != null) {
            for (QuestionImage questionImage : question.getQuestionImages()) {
                fileService.deleteFileByFileName(questionImage.getImageUrl());
            }
        }

        questionRepository.delete(question);

    }

    @Override
    @Transactional
    public List<QuestionResponse> createMultipleQuestions(UUID assessmentId, List<QuestionRequest> requests) {
        UUID currentUserId = UUID.fromString(
                Optional.ofNullable(JwtUtils.getJwt())
                        .map(JwtClaimAccessor::getSubject)
                        .orElseThrow(() -> new UnauthorizeException("No authentication token found"))
        );

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to create questions in this assessment"));

        AtomicInteger questionOrder = new AtomicInteger(questionRepository.countByAssessment(assessment) + 1);

        return requests.stream()
                .map(request -> {
                    Question question = request.toEntity(questionOrder.getAndIncrement(), assessment);

                    if (request.getQuestionImages() != null) {
                        int imageOrder = 1;
                        for (String imageUrl : request.getQuestionImages()) {
                            QuestionImage questionImage = QuestionImage.builder()
                                    .imageOrder(imageOrder++)
                                    .imageUrl(imageUrl)
                                    .question(question)
                                    .build();
                            question.getQuestionImages().add(questionImage);
                        }
                    }

                    Question saved = questionRepository.saveAndFlush(question);

                    return saved.toResponse();
                })
                .toList();
    }

    @Override
    @Transactional
    public List<QuestionResponse> updateQuestionsBulk(UUID assessmentId, List<UpdateQuestionRequest> requests) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndCreatedBy(assessmentId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to modify question in this assessment"));

        return requests.stream()
                .map(request -> {
                    Question question = getOrThrow(assessment.getAssessmentId(), request.getQuestionId());

                    question.setText(request.getText());
                    question.setPoints(request.getPoints());
                    question.setMode(request.getMode());
                    question.setQuestionType(request.getQuestionType());

                    question.getQuestionImages().removeAll(question.getQuestionImages());

                    if (request.getQuestionImages() != null) {
                        int imageOrder = 1;
                        for (String imageUrl : request.getQuestionImages()) {
                            QuestionImage questionImage = QuestionImage.builder()
                                    .imageOrder(imageOrder++)
                                    .imageUrl(imageUrl)
                                    .question(question)
                                    .build();
                            question.getQuestionImages().add(questionImage);
                        }
                    }

                    Question saved = questionRepository.saveAndFlush(question);

                    return saved.toResponse();
                })
                .toList();
    }

    private Question getOrThrow(UUID assessmentId, UUID questionId) {
        return questionRepository
                .findByAssessment_AssessmentId_AndQuestionId(assessmentId, questionId)
                .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));
    }
}
