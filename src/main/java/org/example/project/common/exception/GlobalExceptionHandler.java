package org.example.project.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.R;
import org.example.project.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * REST コントローラ全体の例外を一元的にハンドリングする。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ビジネス例外を処理する。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R> handleBusinessException(BusinessException ex) {
        log.warn("business error: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * 入力検証例外を処理する。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<R> handleValidationException(Exception ex) {
        String message = ErrorCode.VALIDATION_FAILED.getDefaultMessage();
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
            if (manve.getBindingResult().getFieldError() != null) {
                message = manve.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (ex instanceof BindException) {
            BindException bindException = (BindException) ex;
            if (bindException.getBindingResult().getFieldError() != null) {
                message = bindException.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        log.warn("validation error: {}", message);
        return buildErrorResponse(ErrorCode.VALIDATION_FAILED, message, HttpStatus.BAD_REQUEST);
    }

    /**
     * ConstraintViolationException を処理する。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("constraint violation: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.VALIDATION_FAILED, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * システム例外を処理する。
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<R> handleSystemException(SystemException ex) {
        log.error("system error", ex);
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 定義されていない例外のフォールバック。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R> handleUnhandledException(Exception ex) {
        log.error("unhandled error", ex);
        return buildErrorResponse(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getDefaultMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<R> buildErrorResponse(ErrorCode errorCode, String message, HttpStatus status) {
        R body = R.error(errorCode.getCode(), message);
        return ResponseEntity.status(status).body(body);
    }
}

