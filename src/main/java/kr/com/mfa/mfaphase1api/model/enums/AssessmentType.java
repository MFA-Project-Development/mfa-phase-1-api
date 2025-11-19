package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AssessmentType {

    TEST("test"),
    EXAM("exam"),
    HOMEWORK("homework"),
    QUIZ("quiz");

    private final String type;

    AssessmentType(String type) {
        this.type = type;
    }

}
