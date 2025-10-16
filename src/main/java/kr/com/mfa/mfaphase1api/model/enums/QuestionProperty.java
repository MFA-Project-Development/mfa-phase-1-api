package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum QuestionProperty {

    QUESTION_ID("questionId"),
    TITLE("text"),
    QUESTION_ORDER("questionOrder"),
    POINTS("points"),
    CREATED_AT("createdAt");

    private final String property;

    QuestionProperty(String property) {
        this.property = property;
    }


}
