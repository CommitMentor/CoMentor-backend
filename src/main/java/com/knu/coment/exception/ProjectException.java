package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class ProjectException extends BusinessException {

    public ProjectException(ErrorCode code) {
        super(code);
    }
}
