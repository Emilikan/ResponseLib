package ru.emilnasyrov.lib.unitpay.handler;

import ru.emilnasyrov.lib.unitpay.modules.ExceptionDateResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.modules.Locals;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sysout.annotationuse.DefaultException;
import ru.sysout.annotationuse.MHttpException;
import ru.sysout.annotationuse.ErrorAnswerForUnitpay;
import ru.sysout.annotationuse.MyException;
import ru.sysout.annotationuse.MyResponse;

@ControllerAdvice
public class AwesomeExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(DefaultException.class)
    private ResponseEntity<?> handleDefaultException() {
       return new ExceptionDateResponse().generateError(
               HttpStatus.INTERNAL_SERVER_ERROR,
               300,
               "Ошибка с дефолтной кофигурацией",
               Locals.RUS
               );
    }

    @ExceptionHandler(MHttpException.class)
    private ResponseEntity<?> handleMHttpException() {
       return new ErrorAnswerForUnitpay().generateError(
               HttpStatus.INTERNAL_SERVER_ERROR,
               333,
               "Какая-то ошибка (MHttpException)",
               Locals.RUS
               );
    }

    @ExceptionHandler(MyException.class)
    private ResponseEntity<?> handleMyException() {
       return new MyResponse().generateError(
               HttpStatus.ACCEPTED,
               400,
               "Ошибка MyException",
               Locals.RUS
               );
    }
}
