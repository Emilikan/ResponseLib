package ru.sysout.annotationuse;

import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

@HttpException(
        code = 333,
        message = "Какая-то ошибка (MHttpException)",
        responseClass = ErrorAnswerForUnitpay.class
)
public class MHttpException extends RuntimeException {
    private String message;

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    MHttpException(String message){
        this.message = message;
    }
}
