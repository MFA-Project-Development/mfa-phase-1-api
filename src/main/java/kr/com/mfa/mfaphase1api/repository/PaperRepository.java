package kr.com.mfa.mfaphase1api.repository;

import kr.com.mfa.mfaphase1api.model.entity.Paper;
import kr.com.mfa.mfaphase1api.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperRepository extends JpaRepository<Paper, UUID> {
    int countPaperBySubmission(Submission submission);

    List<Paper> findAllBySubmission(Submission submission);

    void deleteAllBySubmission(Submission submission);
}
