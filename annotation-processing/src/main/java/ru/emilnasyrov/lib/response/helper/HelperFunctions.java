package ru.emilnasyrov.lib.response.helper;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class HelperFunctions {
    /**
     * Отправка error-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param messager Messager
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    public static void error(Element e, Messager messager, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    /**
     * Отправка warning-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param messager Messager
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    public static void warning(Element e, Messager messager, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(msg, args),
                e);
    }

    /**
     * Приведение первого символа строки к нижнему регистру
     *
     * @param s строка
     * @return приведенная строка
     */
    public static String toLowerCaseFirstLetter (String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Приведение TypeMirror к TypeElement
     *
     * @param typeMirror элемент, который необходимо привести
     * @return
     */
    public static TypeElement asTypeElement(Types typeUtils, TypeMirror typeMirror) {
        return (TypeElement) typeUtils.asElement(typeMirror);
    }
}
