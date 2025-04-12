package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionExceptionHandler extends RuntimeException {
    private final ErrorCode errorCode;

    @Builder
    public QuestionExceptionHandler(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    @Builder
    public QuestionExceptionHandler(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
