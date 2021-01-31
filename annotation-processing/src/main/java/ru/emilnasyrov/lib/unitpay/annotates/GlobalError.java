package ru.emilnasyrov.lib.unitpay.annotates;

public @interface GlobalError {
    boolean turnOn();
    String message() default "";
    int importance() default 0;
}
