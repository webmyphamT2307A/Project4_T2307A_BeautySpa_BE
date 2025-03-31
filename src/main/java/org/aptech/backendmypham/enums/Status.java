package org.aptech.backendmypham.enums;

import lombok.Getter;

@Getter
public enum Status {
    SUCCESS("success"),
    FAIL("fail"),
    ERROR("error"),
    NOT_FOUND("not_found"),
    INVALID("invalid"),
    UNAUTHORIZED("unauthorized");

    private final String value;

    Status(String value) {
        this.value = value;
    }

}
