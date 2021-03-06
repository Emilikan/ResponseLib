package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.ErrorAnswerForUnitpay;
import ru.emilnasyrov.lib.response.annotates.HttpException;

@HttpException(
        code = 300,
        message = "Какая-то ошибка (UnitpayException)",
        responseClass = ErrorAnswerForUnitpay.class
)
public class UnitpayException extends RuntimeException {
}
