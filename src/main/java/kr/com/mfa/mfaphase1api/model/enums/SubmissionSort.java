package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum SubmissionSort {
    LATEST_WORK("Latest work"),
    OLDEST_WORK("Oldest work"),
    HIGHEST_SCORE("Highest score");

    private final String label;

    SubmissionSort(String label) {
        this.label = label;
    }
}

