package ru.emilnasyrov.lib.unitpay.annotates;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface GlobalError {
    boolean turnOn();
    String message() default "";
    int importance() default 0;
}
