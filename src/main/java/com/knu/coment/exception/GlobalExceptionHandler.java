package com.knu.coment.exception;

import com.knu.coment.exception.code.ErrorCode;
import com.knu.coment.global.code.Api_Response;
import com.knu.coment.global.code.CommonErrorCode;
import com.knu.coment.util.ApiResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
public class GlobalExceptionHandler {   // 상위 10줄만

    /* ── 스택트레이스 문자열 ── */
    private String trace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /* ── 1) 비즈니스 예외 ── */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Api_Response<Object>> handleBusiness(
            BusinessException ex, HttpServletRequest req) {

        ErrorCode code = ex.getErrorCode();
        log.warn("[BUSINESS] {} {} | {}", req.getMethod(), req.getRequestURI(), code.getMessage());

        return ApiResponseUtil.error(code);          // ApiResponseUtil.error(ErrorCode)
    }

    /* ── 2) 무결성 위반 ── */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Api_Response<Object>> handleIntegrity(DataIntegrityViolationException ex) {

        String msg = "필수 필드에 null이 들어갔습니다: ";
        String detail = ex.getMostSpecificCause().getMessage();
        if (detail != null) {
            Matcher m = Pattern.compile("Column '(.*?)' cannot be null").matcher(detail);
            msg += m.find() ? m.group(1) : "알 수 없는 필드";
        }
        return ApiResponseUtil.error(msg, CommonErrorCode.BAD_REQUEST.getHttpStatus());
    }

    /* ── 3) DTO 검증 ── */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Api_Response<Object>> handleValidation(MethodArgumentNotValidException ex) {

        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .findFirst()
                .orElse(CommonErrorCode.BAD_REQUEST.getMessage());

        return ApiResponseUtil.error(msg, CommonErrorCode.BAD_REQUEST.getHttpStatus());
    }

    /* ── 4) 예상 밖 모든 예외 ── */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Api_Response<Object>> handleUnknown(Exception ex, HttpServletRequest req) {

        log.error("[UNEXPECTED] {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ApiResponseUtil.error(firstLines(trace(ex),10), CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
    private String firstLines(String trace, int lines) {
        return Arrays.stream(trace.split("\\R"))
                .limit(lines)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
