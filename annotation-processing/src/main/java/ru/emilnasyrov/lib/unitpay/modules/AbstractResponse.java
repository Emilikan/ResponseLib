package ru.emilnasyrov.lib.unitpay.modules;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

abstract public class AbstractResponse {
    public abstract ResponseEntity<?> generateError(HttpStatus status, int code, String message, Locals locals);
}
