package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.SubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubjectResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubjectProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface SubjectService {

    SubjectResponse createSubject(SubjectRequest request);

    PagedResponse<List<SubjectResponse>> getAllSubjects(Integer page, Integer size, SubjectProperty property, Sort.Direction direction);

    SubjectResponse getSubjectById(UUID subjectId);

    SubjectResponse updateSubjectById(UUID subjectId, SubjectRequest request);

    void deleteSubjectById(UUID subjectId);
}
