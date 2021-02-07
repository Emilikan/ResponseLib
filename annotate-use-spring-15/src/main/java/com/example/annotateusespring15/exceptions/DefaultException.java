package com.example.annotateusespring15.exceptions;

import ru.emilnasyrov.lib.response.annotates.HttpException;

@HttpException(
        code = 300,
        message = "Ошибка с дефолтной кофигурацией")
public class DefaultException extends RuntimeException{
}
