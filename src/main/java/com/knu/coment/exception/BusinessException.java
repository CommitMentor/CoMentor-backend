package com.knu.coment.exception;

import com.knu.coment.global.code.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    protected BusinessException(ErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }
}
