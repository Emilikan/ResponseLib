package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.MyResponse;
import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

@HttpException(
        code = 400,
        message = "Ошибка MyException",
        status = HttpStatus.ACCEPTED,
        responseClass = MyResponse.class
)
public class MyExceptions extends RuntimeException {

    @HttpException(code = 301, message = "Приватная ошибка")
    public class PrivateException extends RuntimeException {

    }
}
