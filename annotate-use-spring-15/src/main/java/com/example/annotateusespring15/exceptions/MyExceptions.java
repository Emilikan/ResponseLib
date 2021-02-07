package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.MyResponse;
import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.response.annotates.GlobalError;
import ru.emilnasyrov.lib.response.annotates.HttpException;
import ru.emilnasyrov.lib.response.modules.AbstractException;

@HttpException(
        code = 400,
        message = "Ошибка MyException",
        status = HttpStatus.ACCEPTED,
        responseClass = MyResponse.class,
        globalError = @GlobalError(turnOn = true, message = "Сервисное сообщение", importance = 1)
)
public class MyExceptions extends AbstractException {

    @HttpException(code = 301, message = "Приватная ошибка")
    public class PrivateException extends RuntimeException {

    }
}
