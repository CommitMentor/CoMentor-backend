package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class QuestionException extends BusinessException {

    public QuestionException(ErrorCode code) {
        super(code);
    }
}


