package com.ajith.codejudge.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends BaseException {
    public AiServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_GATEWAY);
    }
}
