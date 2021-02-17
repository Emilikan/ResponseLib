package com.example.gradleannotateuse15.exceptions;

import com.example.gradleannotateuse15.response.ErrorAnswerForUnitpay;
import ru.emilnasyrov.lib.response.annotates.HttpException;

@HttpException(
        code = 333,
        message = "Какая-то ошибка (UnitpayException)",
        responseClass = ErrorAnswerForUnitpay.class
)
public class UnitpayException extends RuntimeException {
}
