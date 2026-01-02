package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AnswerProperty {

    ANSWER_ID("answerId"),
    POINTS_AWARDED("pointsAwarded"),
    CREATED_AT("createdAt");

    private final String property;

    AnswerProperty(String property) {
        this.property = property;
    }

}
