package com.example.gradleannotateuse15.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.emilnasyrov.lib.response.modules.AbstractResponse;
import ru.emilnasyrov.lib.response.modules.Locals;

import java.util.HashMap;

public class ErrorAnswerForUnitpay extends AbstractResponse {
    private final HashMap<String, String> error;

    public ErrorAnswerForUnitpay(String message) {
        this.error = new HashMap<>();
        error.put("message", message);
    }

    public ErrorAnswerForUnitpay(){this.error = null;}

    public HashMap<String, String> getError() { return error; }

    @Override
    public ResponseEntity<?> generateError(HttpStatus httpStatus, int code, String message, Locals local) {
        return new ResponseEntity<>(new ErrorAnswerForUnitpay(message), httpStatus);
    }
}
