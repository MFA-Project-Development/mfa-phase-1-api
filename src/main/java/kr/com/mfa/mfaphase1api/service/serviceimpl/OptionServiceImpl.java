package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ForbiddenException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.exception.UnauthorizeException;
import kr.com.mfa.mfaphase1api.model.dto.request.OptionRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.OptionResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.entity.Option;
import kr.com.mfa.mfaphase1api.model.entity.Question;
import kr.com.mfa.mfaphase1api.model.enums.OptionProperty;
import kr.com.mfa.mfaphase1api.repository.OptionRepository;
import kr.com.mfa.mfaphase1api.repository.QuestionRepository;
import kr.com.mfa.mfaphase1api.service.OptionService;
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
public class OptionServiceImpl implements OptionService {

    private final OptionRepository optionRepository;
    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public OptionResponse createOption(UUID questionId, OptionRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Question question = questionRepository
                .findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                .orElseThrow(() -> new NotFoundException("You are not authorized to create option in this question"));

        int optionOrder = optionRepository.countByQuestion(question) + 1;

        Option saved = optionRepository.saveAndFlush(request.toEntity(optionOrder, question));

        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<OptionResponse>> getAllOptions(UUID questionId, Integer page, Integer size, OptionProperty property, Sort.Direction direction) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Question question;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> question = questionRepository
                    .findById(questionId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            case "ROLE_INSTRUCTOR" -> question = questionRepository
                    .findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            case "ROLE_STUDENT" -> question = questionRepository
                    .findByQuestionId_AndAssessment_ClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(questionId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Option> pageOptions = optionRepository.findAllByQuestion_QuestionId(question.getQuestionId(), pageable);


        List<OptionResponse> items = pageOptions
                .getContent()
                .stream()
                .map(Option::toResponse)
                .toList();

        return pageResponse(
                items,
                pageOptions.getTotalElements(),
                page,
                size,
                pageOptions.getTotalPages()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public OptionResponse getOptionById(UUID questionId, UUID optionId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
        List<String> currentUserRole = JwtUtils.getJwt().getClaimAsStringList("roles");

        Question question;

        switch (currentUserRole.getFirst()) {
            case "ROLE_ADMIN" -> question = questionRepository
                    .findById(questionId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            case "ROLE_INSTRUCTOR" -> question = questionRepository
                    .findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            case "ROLE_STUDENT" -> question = questionRepository
                    .findByQuestionId_AndAssessment_ClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(questionId, currentUserId)
                    .orElseThrow(() -> new NotFoundException("Question " + questionId + " not found"));

            default -> throw new ForbiddenException("Unsupported role: " + currentUserRole.getFirst());

        }

        Option option = optionRepository.findAllByQuestion_QuestionId_AndOptionId(question.getQuestionId(), optionId)
                .orElseThrow(() -> new NotFoundException("Option " + optionId + " not found"));

        return option.toResponse();
    }

    @Override
    @Transactional
    public OptionResponse updateOptionById(UUID questionId, UUID optionId, OptionRequest request) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Question question = questionRepository
                .findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                .orElseThrow(() -> new NotFoundException("You are not authorized to update option in this question"));

        Option option = optionRepository.findAllByQuestion_QuestionId_AndOptionId(question.getQuestionId(), optionId)
                .orElseThrow(() -> new NotFoundException("Option " + optionId + " not found"));

        option.setText(request.getText());
        option.setIsCorrect(request.getIsCorrect());

        return option.toResponse();
    }

    @Override
    @Transactional
    public void deleteOptionById(UUID questionId, UUID optionId) {
        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Question question = questionRepository
                .findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                .orElseThrow(() -> new NotFoundException("You are not authorized to delete option in this question"));

        Option option = optionRepository.findAllByQuestion_QuestionId_AndOptionId(question.getQuestionId(), optionId)
                .orElseThrow(() -> new NotFoundException("Option " + optionId + " not found"));

        optionRepository.delete(option);
    }

    @Override
    @Transactional
    public List<OptionResponse> createMultipleOptions(UUID questionId, List<OptionRequest> requests) {
        UUID currentUserId = UUID.fromString(
                Optional.ofNullable(JwtUtils.getJwt())
                        .map(JwtClaimAccessor::getSubject)
                        .orElseThrow(() -> new UnauthorizeException("No authentication token found"))
        );

        Question question = questionRepository.findByQuestionId_AndAssessment_CreatedBy(questionId, currentUserId)
                .orElseThrow(() -> new ForbiddenException("You are not authorized to create options in this question"));

        AtomicInteger optionOrder = new AtomicInteger(optionRepository.countByQuestion(question) + 1);

        return requests.stream()
                .map(request -> {
                    Option savedOption = optionRepository.saveAndFlush(request.toEntity(optionOrder.getAndIncrement(), question));
                    return savedOption.toResponse();
                })
                .toList();
    }


}
