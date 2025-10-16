package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum GradingMode {

    AUTO("auto"),
    MANUAL("manual"),
    RUBRIC("rubric"),
    AI_ASSISTED("ai_assisted");

    private final String mode;

    GradingMode(String mode) {
        this.mode = mode;
    }
}
