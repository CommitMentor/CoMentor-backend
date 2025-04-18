package com.knu.coment.exception;

import com.knu.coment.global.code.CommonErrorCode;
import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;

/** 모든 커스텀 예외의 베이스 클래스 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final CommonErrorCode errorCode;   // ✅ enum 타입 고정

    protected BusinessException(CommonErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }

    /** 공통 인터페이스 반환용 */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
