package com.knu.coment.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FolderErrorCode implements ErrorCode {

    NOT_FOUND_FOLDER(HttpStatus.NOT_FOUND, "폴더를 찾을 수 없습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "Required field is missing."),
    DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST, "중복된 폴더 이름입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"폴더에 권한이 없습니다");



    private final HttpStatus httpStatus;
    private final String message;
}

