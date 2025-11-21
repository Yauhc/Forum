package org.example.project.common.exception;

import org.example.project.common.error.ErrorCode;

/**
 * システム的な障害を表す例外クラス。
 */
public class SystemException extends RuntimeException {
    private final ErrorCode errorCode;

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

