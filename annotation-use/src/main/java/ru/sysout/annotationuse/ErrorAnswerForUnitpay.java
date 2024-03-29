package ru.sysout.annotationuse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.emilnasyrov.lib.unitpay.modules.AbstractResponse;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

import java.util.HashMap;

/**
 * Проба из транзакций
 */
public class ErrorAnswerForUnitpay extends AbstractResponse {
    private final HashMap<String, String> error;

    public ErrorAnswerForUnitpay(String message) {
        this.error = new HashMap<>();
        error.put("message", message);
    }

    public ErrorAnswerForUnitpay(){this.error = null;}

    public HashMap<String, String> getError() { return error; }

    @Override
    public ResponseEntity<?> generateError(HttpStatus httpStatus, int i, String s, Locals locals) {
        return new ResponseEntity<>(new ErrorAnswerForUnitpay(s), httpStatus);
    }
}
