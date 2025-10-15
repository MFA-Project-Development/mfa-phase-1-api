package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.dto.request.*;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubjectResponse;
import kr.com.mfa.mfaphase1api.model.entity.Subject;
import kr.com.mfa.mfaphase1api.model.enums.SubjectProperty;
import kr.com.mfa.mfaphase1api.repository.SubjectRepository;
import kr.com.mfa.mfaphase1api.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static kr.com.mfa.mfaphase1api.utils.ResponseUtil.pageResponse;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {

        assertNameUnique(request.getName());
        Subject saved = subjectRepository.save(request.toEntity());

        return saved.toResponse();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<List<SubjectResponse>> getAllSubjects(
            Integer page,
            Integer size,
            SubjectProperty property,
            Sort.Direction direction
    ) {

        int zeroBased = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(zeroBased, size, Sort.by(direction, property.getProperty()));
        Page<Subject> pageSubjects = subjectRepository.findAll(pageable);

        List<SubjectResponse> items = pageSubjects
                .getContent()
                .stream()
                .map(Subject::toResponse)
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
    public SubjectResponse getSubjectById(UUID subjectId) {
        Subject subject = getOrThrow(subjectId);
        return subject.toResponse();
    }

    @Override
    @Transactional
    public SubjectResponse updateSubjectById(UUID subjectId, SubjectRequest request) {
        getOrThrow(subjectId);
        assertNameUnique(request.getName());
        Subject saved = subjectRepository.save(request.toEntity(subjectId));
        return saved.toResponse();
    }

    @Override
    @Transactional
    public void deleteSubjectById(UUID subjectId) {
        getOrThrow(subjectId);
        subjectRepository.deleteById(subjectId);
    }

    private Subject getOrThrow(UUID subjectId) {
        return subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new NotFoundException("Subject " + subjectId + "not found"));
    }

    private void assertNameUnique(String name) {
        boolean exists =  subjectRepository.existsByNameIgnoreCase(name);

        if (exists) {
            throw new ConflictException("Subject already exists");
        }
    }

}