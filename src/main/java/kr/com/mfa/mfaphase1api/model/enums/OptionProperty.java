package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum OptionProperty {

    OPTION_ID("optionId"),
    TEXT("text"),
    OPTION_ORDER("optionOrder"),
    CREATED_AT("createdAt");

    private final String property;

    OptionProperty(String property) {
        this.property = property;
    }

}
