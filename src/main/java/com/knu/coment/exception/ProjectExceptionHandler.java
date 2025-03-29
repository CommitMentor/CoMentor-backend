package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ProjectExceptionHandler extends RuntimeException {
    private final ErrorCode errorCode;

    @Builder
    public ProjectExceptionHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    @Builder
    public ProjectExceptionHandler(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
