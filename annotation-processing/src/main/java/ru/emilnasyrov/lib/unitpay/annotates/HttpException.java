package ru.emilnasyrov.lib.unitpay.annotates;

import ru.emilnasyrov.lib.unitpay.modules.AbstractResponse;
import ru.emilnasyrov.lib.unitpay.modules.ExceptionDateResponse;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.HttpStatus;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface HttpException {
    HttpStatus status() default HttpStatus.INTERNAL_SERVER_ERROR;
    int code();
    String message();
    Locals locals() default Locals.RUS;
    Class<? extends AbstractResponse> responseClass() default ExceptionDateResponse.class;
    GlobalError addGlobalError() default @GlobalError(turnOn = false);
}
