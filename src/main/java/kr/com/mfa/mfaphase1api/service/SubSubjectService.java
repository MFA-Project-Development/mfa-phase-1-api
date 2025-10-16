package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.SubSubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubSubjectResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubSubjectProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface SubSubjectService {

    SubSubjectResponse createSubSubject(UUID subjectId, SubSubjectRequest request);

    PagedResponse<List<SubSubjectResponse>> getAllSubSubjects(UUID subjectId, Integer page, Integer size, SubSubjectProperty property, Sort.Direction direction);

    SubSubjectResponse getSubSubjectById(UUID subjectId, UUID subSubjectId);

    SubSubjectResponse updateSubSubjectById(UUID subjectId, UUID subSubjectId, SubSubjectRequest request);

    void deleteSubSubjectById(UUID subjectId, UUID subSubjectId);
}
