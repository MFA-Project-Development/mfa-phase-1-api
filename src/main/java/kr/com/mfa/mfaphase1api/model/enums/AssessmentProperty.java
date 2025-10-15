package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AssessmentProperty {

    ASSESSMENT_ID("assessmentId"),
    TITLE("title"),
    TIME_LIMIT("timeLimit"),
    CREATED_AT("createdAt");

    private final String property;

    AssessmentProperty(String property) {
        this.property = property;
    }

}
