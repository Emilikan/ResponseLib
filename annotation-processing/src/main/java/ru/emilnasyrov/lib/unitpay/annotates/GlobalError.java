package ru.emilnasyrov.lib.unitpay.annotates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface GlobalError {
    boolean turnOn();
    String message() default "";
    int importance() default 0;
}
