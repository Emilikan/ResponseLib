package com.example.annotateusespring15.exceptions;

import com.example.annotateusespring15.response.ErrorAnswerForUnitpay;
import ru.emilnasyrov.lib.unitpay.annotates.GlobalError;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;
import ru.emilnasyrov.lib.unitpay.modules.AbstractException;

@HttpException(
        code = 333,
        message = "Какая-то ошибка (MHttpException)",
        responseClass = ErrorAnswerForUnitpay.class,
        addGlobalError = @GlobalError(
                turnOn = true,
                message = "Сообщение",
                importance = 1
        )
)
public class UnitpayException extends AbstractException {
}
