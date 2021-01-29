package ru.emilnasyrov.lib.unitpay.modules;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AbstractException extends RuntimeException {
    private final StackTraceElement[] stackTraceElements;
    private final String message;

    public AbstractException(Exception e) {
        this.stackTraceElements = e.getStackTrace();
        this.message = e.getMessage();
    }

    public AbstractException(Exception e, String message) {
        this.stackTraceElements = e.getStackTrace();
        this.message = message + " Описание ошибки: " + e.getMessage();
    }

    public AbstractException(String message) {
        this.stackTraceElements = Thread.currentThread().getStackTrace();
        this.message = message;
    }

    public AbstractException() {
        this.stackTraceElements = Thread.currentThread().getStackTrace();
        this.message = null;
    }
}
