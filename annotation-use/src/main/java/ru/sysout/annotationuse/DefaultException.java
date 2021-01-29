package ru.sysout.annotationuse;

import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

@HttpException(
        code = 300,
        message = "Ошибка с дефолтной кофигурацией")
public class DefaultException extends RuntimeException {
}
