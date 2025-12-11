package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubmissionProperty {

    SUBMISSION_ID("submissionId"),
    STATUS("status"),
    MAX_SCORE("maxScore"),
    SCORE_EARNED("scoreEarned"),
    STARTED_AT("startedAt"),
    SUBMITTED_AT("submittedAt"),
    GRADED_AT("gradedAt");

    private final String property;

    SubmissionProperty(String property) {
        this.property = property;
    }

}
