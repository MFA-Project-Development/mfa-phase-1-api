package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum ClassSubSubjectProperty {

    SUB_SUBJECT_ID("subSubject.subSubjectId"), NAME("subSubject.name");

    private final String property;

    ClassSubSubjectProperty(String property) {
        this.property = property;
    }

}
