package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum BaseRole {

    ROLE_STUDENT("STUDENT"),
    ROLE_INSTRUCTOR("INSTRUCTOR");

    private final String roleName;

    BaseRole(String roleName) {
        this.roleName = roleName;
    }

}
