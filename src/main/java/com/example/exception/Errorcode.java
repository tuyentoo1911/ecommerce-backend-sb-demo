package com.example.exception;

import org.springframework.http.HttpStatusCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Errorcode {
    USER_EXISTS(1001, "User already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1002, "Username must be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003, "Password must be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1004, "Invalid key", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1005, "Invalid email address", HttpStatus.BAD_REQUEST),
    FIELD_REQUIRED(1006, "Field is required", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1007, "User not found", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1008, "Invalid password", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1011, "Authentication required - Please provide valid JWT token", HttpStatus.UNAUTHORIZED),
    UNCATEGORIZED_ERROR(9999, "Uncategorized error", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1009, "you are not authorized to access this resource", HttpStatus.FORBIDDEN),
    DOB_INVALID(1010, "your age must be at least {min}", HttpStatus.BAD_REQUEST);

    private int code;
    private String message;
    private HttpStatusCode statuscode;

    Errorcode(int code, String message, HttpStatusCode statuscode) {
        this.code = code;
        this.message = message;
        this.statuscode = statuscode;
    }
}
