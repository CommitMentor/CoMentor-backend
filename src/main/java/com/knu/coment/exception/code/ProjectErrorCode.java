package com.knu.coment.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProjectErrorCode implements ErrorCode {

    // 404
    NOT_FOUND_PROJECT(HttpStatus.NOT_FOUND, "Project with the specified ID was not found."),
    NOT_FOUND_Repo(HttpStatus.NOT_FOUND, "해당 레포를 찾을 수 없습니다"),

    // 400
    INVALID_PROJECT_ID(HttpStatus.BAD_REQUEST, "Invalid project ID provided."),
    INVALID_PROJECT_DATA(HttpStatus.BAD_REQUEST, "Invalid project data provided."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "Required field is missing."),

    // 403
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "You do not have permission to perform this action on the project."),

    // 500
    UPDATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the project information."),
    INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while inserting the project information."),
    SELECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while selecting the project information."),

    // 409
    PROJECT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Project already exists or conflicts with existing data.");

    private final HttpStatus httpStatus;
    private final String message;
}
