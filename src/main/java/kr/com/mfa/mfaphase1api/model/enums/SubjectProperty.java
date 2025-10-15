package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubjectProperty {

    SUBJECT_ID("subjectId"), NAME("name");

    private final String property;

    SubjectProperty(String property) {
        this.property = property;
    }

}
