package com.knu.coment.util;

import com.knu.coment.exception.code.ErrorCode;
import com.knu.coment.global.code.Api_Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseUtil {

    /* ───────── 공통 빌더 ───────── */
    private static <T> ResponseEntity<Api_Response<T>> build(
            HttpStatus status, String msg, T data) {

        Api_Response<T> body = Api_Response.<T>builder()
                .code(status.value())
                .message(msg)
                .result(data)
                .build();

        return ResponseEntity.status(status).body(body);
    }

    /* ───────── 성공 응답 ───────── */
    public static <T> ResponseEntity<Api_Response<T>> ok(String msg, T data) {
        return build(HttpStatus.OK, msg, data);
    }
    public static <T> ResponseEntity<Api_Response<T>> ok(String msg) {
        return ok(msg, null);
    }

    /* ───────── 오류 응답 ───────── */

    public static <T> ResponseEntity<Api_Response<T>> error(ErrorCode ec) {
        return build(ec.getHttpStatus(), ec.getMessage(), null);
    }

    public static <T> ResponseEntity<Api_Response<T>> error(String msg, HttpStatus st) {
        return build(st, msg, null);
    }
}
