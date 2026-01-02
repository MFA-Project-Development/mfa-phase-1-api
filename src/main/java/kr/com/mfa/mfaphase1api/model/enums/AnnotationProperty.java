package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AnnotationProperty {

    ANNOTATION_ID("annotationId"),
    CREATED_AT("createdAt");

    private final String property;

    AnnotationProperty(String property) {
        this.property = property;
    }

}
