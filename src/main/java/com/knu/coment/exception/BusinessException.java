package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;

/** 모든 커스텀 예외의 베이스 클래스 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode  errorCode;

    protected BusinessException(ErrorCode  code) {
        super(code.getMessage());
        this.errorCode = code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
