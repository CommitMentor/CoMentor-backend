package com.knu.coment.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnswerErrorCode implements ErrorCode {

    NOT_FOUND_ANSWER(HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다."),
    ALREADY_DONE_ANSWER(HttpStatus.BAD_REQUEST, "이미 답변이 완료된 질문입니다."),
    UNAUTHORIZED_QUESTION_ACCESS(HttpStatus.FORBIDDEN, "해당 질문에 대한 권한이 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
