package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum ClassProperty {

    CLASS_ID("classId"), NAME("name"), CODE("code");

    private final String property;

    ClassProperty(String property) {
        this.property = property;
    }

}
