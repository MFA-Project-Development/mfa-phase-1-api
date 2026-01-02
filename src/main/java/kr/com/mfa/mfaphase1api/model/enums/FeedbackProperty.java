package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum FeedbackProperty {

    FEEDBACK_ID("feedbackId"),
    COMMENT("comment"),
    CREATED_AT("createdAt");

    private final String property;

    FeedbackProperty(String property) {
        this.property = property;
    }

}
