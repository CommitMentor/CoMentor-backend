package com.knu.coment.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    // 404
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "User with the specified ID was not found."),

    // 400
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "Invalid user ID provided."),
    INVALID_USER_DATA(HttpStatus.BAD_REQUEST, "Invalid user data provided."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "Required field is missing."),

    // 403
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),

    // 500 (Internal Server Error)
    UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the user information."),
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while inserting the user information."),
    SELECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while selecting the user information."),

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
