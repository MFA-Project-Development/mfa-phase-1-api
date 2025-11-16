package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.AdminOverviewResponse;
import kr.com.mfa.mfaphase1api.model.enums.BaseRole;
import kr.com.mfa.mfaphase1api.repository.ClassRepository;
import kr.com.mfa.mfaphase1api.repository.SubSubjectRepository;
import kr.com.mfa.mfaphase1api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserClient userClient;
    private final SubSubjectRepository subSubjectRepository;
    private final ClassRepository classRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getAdminOverview() {

        long totalInstructors = getUserCountByRole(BaseRole.ROLE_INSTRUCTOR);
        long totalStudents = getUserCountByRole(BaseRole.ROLE_STUDENT);
        long totalSubSubjects = subSubjectRepository.count();
        long totalClasses = classRepository.count();

        return AdminOverviewResponse.builder()
                .totalInstructors(totalInstructors)
                .totalStudents(totalStudents)
                .totalClasses(totalClasses)
                .totalSubSubjects(totalSubSubjects)
                .build();
    }

    private long getUserCountByRole(BaseRole role) {
        return Optional.ofNullable(userClient.getAllUsersBaseRole(role))
                .map(ResponseEntity::getBody)
                .map(APIResponse::getPayload)
                .map(List::size)
                .map(Integer::longValue)
                .orElse(0L);
    }

}
