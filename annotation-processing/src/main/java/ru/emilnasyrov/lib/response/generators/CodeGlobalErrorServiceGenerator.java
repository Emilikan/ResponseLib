package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Service;
import ru.emilnasyrov.lib.response.helper.Helper;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class CodeGlobalErrorServiceGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    private JavaFile javaFile = null;

    public CodeGlobalErrorServiceGenerator(Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement){
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.filer = filer;
        this.rootElement = rootElement;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();
    }

    public JavaFile buildJavaFile() {
        JavaFile.Builder builder = JavaFile
                .builder(
                        Helper.getCodeGlobalErrorServicePackage(rootElementPackage),
                        buildType()
                )
                .indent("    ");

        javaFile = builder.build();
        return javaFile;
    }

    public void generate () throws Throwable {
        if (javaFile==null) {
            error(rootElement, "you must first call the buildJavaFile method");
            throw new Exception("you must first call the buildJavaFile method");
        }
        
        try {
            javaFile.writeTo(filer);
        } catch (FilerException e){
            e.printStackTrace();
        }
    }

    public TypeSpec buildType (){
        return TypeSpec.classBuilder(Helper.getCodeGlobalErrorServiceName())
                .addAnnotation(Service.class)
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    /**
     * Отправка error-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    /**
     * Отправка warning-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    private void warning(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(msg, args),
                e);
    }
}
