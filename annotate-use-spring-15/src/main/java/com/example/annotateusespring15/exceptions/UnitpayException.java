package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.ErrorAnswerForUnitpay;
import ru.emilnasyrov.lib.response.annotates.GlobalError;
import ru.emilnasyrov.lib.response.annotates.HttpException;
import ru.emilnasyrov.lib.response.modules.AbstractException;

@HttpException(
        code = 333,
        message = "Какая-то ошибка (UnitpayException)",
        responseClass = ErrorAnswerForUnitpay.class
)
public class UnitpayException extends RuntimeException {
}
