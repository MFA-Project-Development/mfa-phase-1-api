package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum TimeRange {
    CURRENT_MONTH("Current month"),
    LAST_MONTH("Last month"),
    THIS_YEAR("This year");

    private final String label;

    TimeRange(String label) {
        this.label = label;
    }
}


