package kr.com.mfa.mfaphase1api.service.serviceimpl;

import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.entity.Assessment;
import kr.com.mfa.mfaphase1api.model.entity.Paper;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import kr.com.mfa.mfaphase1api.model.enums.SubmissionStatus;
import kr.com.mfa.mfaphase1api.repository.AssessmentRepository;
import kr.com.mfa.mfaphase1api.repository.PaperRepository;
import kr.com.mfa.mfaphase1api.repository.SubmissionRepository;
import kr.com.mfa.mfaphase1api.service.FileService;
import kr.com.mfa.mfaphase1api.service.SubmissionService;
import kr.com.mfa.mfaphase1api.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final PaperRepository paperRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public UUID startSubmission(UUID assessmentId) {
        UUID currentUserId = extractCurrentUserId();

        Assessment assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId).orElseThrow(
                () -> new NotFoundException("Assessment not found")
        );

        if (submissionRepository.existsByAssessment_AssessmentIdAndStudentId(assessmentId, currentUserId)) {
            throw new ConflictException("You have already started submission for this assessment");
        }

        Submission submission = Submission.builder()
                .status(SubmissionStatus.NOT_SUBMITTED)
                .maxScore(BigDecimal.valueOf(0.00))
                .scoreEarned(BigDecimal.valueOf(0.00))
                .assessment(assessment)
                .studentId(currentUserId)
                .build();

        return submissionRepository.save(submission).getSubmissionId();
    }

    @Override
    @Transactional
    public void persistSubmissionPapers(UUID assessmentId, UUID submissionId, List<String> fileNames) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        validateFilesExist(fileNames);

        int startingPage = paperRepository.countPaperBySubmission(submission) + 1;

        List<Paper> papers = IntStream.range(0, fileNames.size())
                .mapToObj(i -> Paper.builder()
                        .page(startingPage + i)
                        .name(fileNames.get(i))
                        .submission(submission)
                        .build())
                .toList();

        paperRepository.saveAll(papers);
    }

    @Override
    @Transactional
    public void finalizeSubmission(UUID assessmentId, UUID submissionId) {
        UUID currentUserId = extractCurrentUserId();

        Submission submission = getAndValidateSubmission(assessmentId, currentUserId);

        if (submission.getStatus() != SubmissionStatus.NOT_SUBMITTED) {
            throw new ConflictException("Submission has already been submitted for this assessment");
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());
        submissionRepository.save(submission);
    }

    private UUID extractCurrentUserId() {
        return UUID.fromString(Objects.requireNonNull(JwtUtils.getJwt()).getSubject());
    }

    private void validateFilesExist(List<String> fileNames) {
        fileNames.forEach(fileService::getFileByFileName);
    }

    private Submission getAndValidateSubmission(UUID assessmentId, UUID currentUserId) {
        Assessment assessment = assessmentRepository.findByAssessmentId_AndClassSubSubjectInstructor_ClassSubSubject_Clazz_StudentClassEnrollments_StudentId(assessmentId, currentUserId)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));

        return submissionRepository.findSubmissionByAssessmentAndStudentId(assessment, currentUserId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
    }

}
