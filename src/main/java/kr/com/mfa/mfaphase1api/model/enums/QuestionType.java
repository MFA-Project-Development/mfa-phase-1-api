package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum QuestionType {

    SINGLE_CHOICE("single_choice"),
    MULTIPLE_CHOICE("multiple_choice"),
    SHORT_ANSWER("short_answer");

    private final String type;

    QuestionType(String type){
        this.type = type;
    }

}
