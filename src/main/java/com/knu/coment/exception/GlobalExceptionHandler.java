package com.knu.coment.exception;

import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.ErrorCode;
import com.knu.coment.util.ApiResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Api_Response<Object>> handle(DataIntegrityViolationException e) {
        String message = "필수 필드에 null이 들어갔습니다: ";

        String exceptionMessage = e.getMostSpecificCause().getMessage();

        if (exceptionMessage != null) {
            // 정규 표현식을 이용해 "Column '필드명' cannot be null" 패턴에서 필드명 추출
            Pattern pattern = Pattern.compile("Column '(.*?)' cannot be null");
            Matcher matcher = pattern.matcher(exceptionMessage);

            if (matcher.find()) {
                String columnName = matcher.group(1);
                message += columnName;
            } else {
                message += "알 수 없는 필드";
            }
        }
        return ApiResponseUtil.createBadRequestResponse(message);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<Api_Response<Object>> handle(RuntimeException e) {
        return ApiResponseUtil.createErrorResponse(
                e.getMessage(),
                ErrorCode.INSERT_ERROR.getStatus()
        );
    }
}
