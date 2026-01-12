package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubmissionStatus {

    NOT_SUBMITTED("not submitted"),
    SUBMITTED("submitted"),
    LATE("late"),
    MISSED("missed"),
    GRADED("graded"),
    PUBLISHED("published"),
    RETURNED("returned"),
    RESUBMITTED("resubmitted"),
    CANCELLED("cancelled"),
    REJECTED("rejected");

    private final String property;

    SubmissionStatus(String property) {
        this.property = property;
    }

}
