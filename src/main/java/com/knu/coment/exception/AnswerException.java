package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class AnswerException extends BusinessException {

    public AnswerException(ErrorCode code) {
        super(code);
    }
}
