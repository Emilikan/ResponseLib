package ru.sysout.annotationuse;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;

@HttpException(
        code = 300,
        message = "Ошибка с дефолтной кофигурацией")
public class DefaultException extends RuntimeException {
}
