package ru.sysout.annotationuse;

import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

@HttpException(
        code = 400,
        message = "Ошибка MyException",
        status = HttpStatus.ACCEPTED,
        responseClass = MyResponse.class
)
public class MyException extends RuntimeException {
}
