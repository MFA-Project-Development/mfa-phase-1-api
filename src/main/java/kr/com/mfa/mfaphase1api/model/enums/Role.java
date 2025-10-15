package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("ADMIN"),
    ROLE_STUDENT("STUDENT"),
    ROLE_INSTRUCTOR("INSTRUCTOR");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }
}
