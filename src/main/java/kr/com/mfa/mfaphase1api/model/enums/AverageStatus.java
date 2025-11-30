package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum AverageStatus {

    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private final String value;

    AverageStatus(String value) {
        this.value = value;
    }
}
