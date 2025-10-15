package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AssessmentStatus {
    STARTED("started"), FINISHED("finished");

    private final String property;

    AssessmentStatus(String property) {
        this.property = property;
    }
}
