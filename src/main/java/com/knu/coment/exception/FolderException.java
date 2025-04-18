package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import lombok.Getter;


@Getter
public class FolderException extends BusinessException {

    public FolderException(ErrorCode code) {
        super(code);
    }
}
