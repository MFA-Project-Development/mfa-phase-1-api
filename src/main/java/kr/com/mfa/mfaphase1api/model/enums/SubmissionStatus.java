package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubmissionStatus {

    NOT_SUBMITTED("not submitted"),
    SUBMITTED("submitted"),
    LATE("late"),
    GRADED("graded"),
    RETURNED("returned"),
    RESUBMITTED("resubmitted"),
    CANCELLED("cancelled");

    private final String property;

    SubmissionStatus(String property) {
        this.property = property;
    }

}
