package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AssessmentTypeProperty {

    ASSESSMENT_TYPE_ID("assessmentTypeId"), TYPE("type");

    private final String property;

    AssessmentTypeProperty(String property) {
        this.property = property;
    }

}
