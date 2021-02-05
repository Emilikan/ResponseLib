package ru.emilnasyrov.lib.unitpay.annotates;

import ru.emilnasyrov.lib.unitpay.modules.AbstractResponse;
import ru.emilnasyrov.lib.unitpay.modules.ExceptionDateResponse;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

import java.lang.annotation.*;

import org.springframework.http.HttpStatus;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface HttpException {
    HttpStatus status() default HttpStatus.INTERNAL_SERVER_ERROR;
    int code();
    String message();
    Locals local() default Locals.RUS;
    Class<? extends AbstractResponse> responseClass() default ExceptionDateResponse.class;
    GlobalError globalError() default @GlobalError(turnOn = false);
}
