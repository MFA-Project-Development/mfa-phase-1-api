package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.SubmissionRepository;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;

    @Override
    public void startSubmission(UUID assessmentId) {

        UUID currentUserId = UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());

        Assessment assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId).orElseThrow(
                () -> new NotFoundException("Assessment not found")
        );

        Submission submission = Submission.builder()
                .status(SubmissionStatus.NOT_SUBMITTED)
                .maxScore(BigDecimal.valueOf(0.00))
                .scoreEarned(BigDecimal.valueOf(0.00))
                .assessment(assessment)
                .studentId(currentUserId)
                .build();

        submissionRepository.save(submission);
    }
}
