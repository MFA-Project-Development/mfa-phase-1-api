package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.request.SubSubjectRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.PagedResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.SubSubjectResponse;
import kr.com.mfa.mfaphase1api.model.enums.SubSubjectProperty;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface SubSubjectService {

    SubSubjectResponse createSubSubject(SubSubjectRequest request);

    PagedResponse<List<SubSubjectResponse>> getAllSubSubjects(Integer page, Integer size, SubSubjectProperty property, Sort.Direction direction);

    SubSubjectResponse getSubSubjectById(UUID subSubjectId);

    SubSubjectResponse updateSubSubjectById(UUID subSubjectId, SubSubjectRequest request);

    void deleteSubSubjectById(UUID subSubjectId);
}
