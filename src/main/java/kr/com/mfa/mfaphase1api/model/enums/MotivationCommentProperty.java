package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum MotivationCommentProperty {

    MOTIVATION_COMMENT_ID("motivationCommentId"),
    COMMENT("comment"),
    CREATED_AT("createdAt");

    private final String property;

    MotivationCommentProperty(String property) {
        this.property = property;
    }

}
