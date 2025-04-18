package com.knu.coment.exception;

import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.ErrorCode;
import com.knu.coment.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /* 1) JPA 제약 오류 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Api_Response<Object>> handleDataIntegrity(DataIntegrityViolationException e) {
        String message = "필수 필드에 null이 들어갔습니다: ";

        String exceptionMessage = e.getMostSpecificCause().getMessage();

        if (exceptionMessage != null) {
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

    /* 2) 비즈니스 로직 커스텀 예외 (403, 404, 409 등) */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Api_Response<Object>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("[{}] {}", code.name(), code.getMessage());
        return ApiResponseUtil.createErrorResponse(code.getMessage(), code.getStatus());
    }

    /* 3) 예기치 못한 모든 예외 → 500 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Api_Response<Object>> handleUnknown(
            Exception e, HttpServletRequest req) {

        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String trace = firstLines(sw.toString(), 10);

        log.error("[UNEXPECTED] {} {}", req.getMethod(), req.getRequestURI(), e);

        return ApiResponseUtil.createErrorResponse(
                trace,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private String firstLines(String trace, int lines) {
        return Arrays.stream(trace.split("\\R"))
                .limit(lines)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}

