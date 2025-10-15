package kr.com.mfa.mfaphase1api.model.enums;

import lombok.Getter;

@Getter
public enum ModuleTypeProperty {

    MODULE_TYPE_ID("moduleTypeId"), TYPE("type");

    private final String property;

    ModuleTypeProperty(String property) {
        this.property = property;
    }

}
