package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum QuestionType {

    SINGLE_CHOICE("single_choice"),
    MULTIPLE_CHOICE("multiple_choice"),
    TEXT("text"),
    TRUE_FALSE("true_false");

    private final String type;

    QuestionType(String type){
        this.type = type;
    }

}
