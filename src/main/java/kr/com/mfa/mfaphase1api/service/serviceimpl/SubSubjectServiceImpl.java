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
    public SubSubjectResponse createSubSubject(SubSubjectRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not " + request.getSubjectId() + " found"));

        if (subSubjectRepository.existsByNameIgnoreCaseAndSubject_SubjectId(request.getName(), subject.getSubjectId())) {
            throw new ConflictException("SubSubject name already exists for this subject");
        }

        SubSubject saved = subSubjectRepository.save(request.toEntity(subject));
        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<SubSubjectResponse>> getAllSubSubjects(Integer page, Integer size, SubSubjectProperty property, Sort.Direction direction) {

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));

        Page<SubSubject> pageSubjects = subSubjectRepository.findAll(pageable);

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
    public SubSubjectResponse getSubSubjectById(UUID subSubjectId) {
        SubSubject subSubject = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not " + subSubjectId + " found"));
        return subSubject.toResponse();
    }

    @Override
    @Transactional
    public SubSubjectResponse updateSubSubjectById(UUID subSubjectId, SubSubjectRequest request) {
        SubSubject existing = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not " + subSubjectId + " found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject not " + request.getSubjectId() + " found"));

        if (subSubjectRepository.existsByNameIgnoreCaseAndSubject_SubjectIdAndSubSubjectIdNot(
                request.getName(), subject.getSubjectId(), subSubjectId)) {
            throw new ConflictException("SubSubject name already exists for this subject");
        }

        existing.setName(request.getName());
        existing.setSubject(subject);

        SubSubject updated = subSubjectRepository.save(existing);
        return updated.toResponse();
    }

    @Override
    @Transactional
    public void deleteSubSubjectById(UUID subSubjectId) {
        SubSubject existing = subSubjectRepository.findById(subSubjectId)
                .orElseThrow(() -> new NotFoundException("SubSubject not " + subSubjectId + " found"));
        subSubjectRepository.delete(existing);
    }

}
