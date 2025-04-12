package com.knu.coment.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestionErrorCode implements ErrorCode {

    NOT_FOUND_QUESTION(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");



    private final HttpStatus httpStatus;
    private final String message;
}
