package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.ErrorAnswerForUnitpay;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

@HttpException(
        code = 333,
        message = "Какая-то ошибка (MHttpException)",
        responseClass = ErrorAnswerForUnitpay.class
)
public class UnitpayException extends RuntimeException {
}
