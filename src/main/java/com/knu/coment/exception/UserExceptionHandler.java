package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserExceptionHandler extends RuntimeException {
    private final ErrorCode errorCode;

    @Builder
    public UserExceptionHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    @Builder
    public UserExceptionHandler(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}