package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum ScoreStatus {

    GOOD("good"),
    AVERAGE("average"),
    LOW("low");

    private final String value;

    ScoreStatus(String value) {
        this.value = value;
    }
}
