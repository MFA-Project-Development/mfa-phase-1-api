package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum MotivationContentProperty {

    CONTENT_ID("motivationContentId"),
    TYPE("type"),
    CREATED_AT("createdAt");

    private final String property;

    MotivationContentProperty(String property) {
        this.property = property;
    }

}
