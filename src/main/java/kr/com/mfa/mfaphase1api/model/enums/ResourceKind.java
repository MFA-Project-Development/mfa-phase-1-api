package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum ResourceKind {

    FILE("file"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    PDF("pdf"),
    DOCUMENT("document"),
    LINK("link"),
    TEXT("text");

    private final String kind;

    ResourceKind(String kind) {
        this.kind = kind;
    }

}
