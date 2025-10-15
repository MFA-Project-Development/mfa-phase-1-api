package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubSubjectProperty {

    SUB_SUBJECT_ID("subSubjectId"), NAME("name");

    private final String property;

    SubSubjectProperty(String property) {
        this.property = property;
    }

}
