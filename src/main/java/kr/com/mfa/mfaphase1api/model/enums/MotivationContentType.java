package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;


@Getter
public enum MotivationContentType {

    QUOTE("quote"),
    VIDEO("video"),
    MESSAGE("message"),
    BOOK("book");

    private final String type;

    MotivationContentType(String type) {
        this.type = type;
    }

}
