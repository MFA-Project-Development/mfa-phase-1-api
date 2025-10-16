package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum QuestionTypeProperty {

    QUESTION_TYPE_ID("questionTypeId"),
    TYPE("type");

    private final String property;

    QuestionTypeProperty(String property) {
        this.property = property;
    }

}
