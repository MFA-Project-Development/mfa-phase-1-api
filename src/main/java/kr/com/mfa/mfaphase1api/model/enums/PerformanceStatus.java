package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum PerformanceStatus {

    TOTAL_AVG_SCORE("totalAvgScore"),
    TOTAL_ASSESSMENT("totalAssessment");

    private final String value;

    PerformanceStatus(String value) {
        this.value = value;
    }

}
