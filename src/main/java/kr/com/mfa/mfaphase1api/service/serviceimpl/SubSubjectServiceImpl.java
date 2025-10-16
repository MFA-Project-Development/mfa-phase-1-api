package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.SubSubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubSubjectResponse;
import kr.com.mfa.mfaphase1api.model.entity.SubSubject;
import kr.com.mfa.mfaphase1api.model.entity.Subject;
import kr.com.mfa.mfaphase1api.model.enums.SubSubjectProperty;
import kr.com.mfa.mfaphase1api.repository.SubSubjectRepository;
import kr.com.mfa.mfaphase1api.repository.SubjectRepository;
import kr.com.mfa.mfaphase1api.service.SubSubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubSubjectServiceImpl implements SubSubjectService {

    private final SubSubjectRepository subSubjectRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public SubSubjectResponse createSubSubject(UUID subjectId, SubSubjectRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Subject not " + subjectId + " found"));

        if (subSubjectRepository.existsByNameIgnoreCaseAndSubject_SubjectId(request.getName(), subjectId)) {
            throw new ConflictException("SubSubject name already exists for this subject");
        }

        SubSubject saved = subSubjectRepository.save(request.toEntity(subject));
        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<SubSubjectResponse>> getAllSubSubjects(UUID subjectId, Integer page, Integer size, SubSubjectProperty property, Sort.Direction direction) {

        if (!subjectRepository.existsById(subjectId)) {
            throw new NotFoundException("Subject " + subjectId + " not found");
        }

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<SubSubject> pageSubjects = subSubjectRepository.findAllBySubject_SubjectId(subjectId, pageable);

        List<SubSubjectResponse> items = pageSubjects
                .getContent()
                .stream()
                .map(SubSubject::toResponse)
                .toList();

        return pageResponse(
                items,
                pageSubjects.getTotalElements(),
                page,
                size,
                pageSubjects.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SubSubjectResponse getSubSubjectById(UUID subjectId, UUID subSubjectId) {
        SubSubject subSubject = subSubjectRepository.findBySubject_SubjectIdAndSubSubjectId(subjectId, subSubjectId)
                .orElseThrow(() -> new NotFoundException(
                        "Sub-subject with ID " + subSubjectId + " not found for subject " + subjectId
                ));

        return subSubject.toResponse();
    }

    @Override
    @Transactional
    public SubSubjectResponse updateSubSubjectById(UUID subjectId, UUID subSubjectId, SubSubjectRequest request) {
        SubSubject existing = subSubjectRepository
                .findBySubject_SubjectIdAndSubSubjectId(subjectId, subSubjectId)
                .orElseThrow(() -> new NotFoundException(
                        "Sub-subject " + subSubjectId + " not found under subject " + subjectId
                ));

        String newName = request.getName().trim();
        if (subSubjectRepository.existsByNameIgnoreCaseAndSubject_SubjectIdAndSubSubjectIdNot(
                newName, subjectId, subSubjectId)) {
            throw new ConflictException("A sub-subject with the same name already exists under this subject.");
        }

        existing.setName(newName);

        SubSubject updated = subSubjectRepository.save(existing);
        return updated.toResponse();
    }

    @Override
    @Transactional
    public void deleteSubSubjectById(UUID subjectId, UUID subSubjectId) {
        SubSubject existing = subSubjectRepository
                .findBySubject_SubjectIdAndSubSubjectId(subjectId, subSubjectId)
                .orElseThrow(() -> new NotFoundException(
                        "Sub-subject " + subSubjectId + " not found under subject " + subjectId
                ));
        subSubjectRepository.delete(existing);
    }

}
