package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.FilesLocales.getGlobalErrorsRepositoryName;

public class NotificationEmailsRepositoryGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    private final ClassName notificationEmailsClassName;

    public NotificationEmailsRepositoryGenerator (Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.messager = messager;
        this.rootElement = rootElement;
        this.typeUtils = typeUtils;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();
        this.notificationEmailsClassName = ClassName.get(getNotificationEmailsPackage(rootElementPackage), getNotificationEmailsName());
    }

    public void generate () throws Throwable {
        JavaFile javaFile = JavaFile
                .builder(
                        getNotificationEmailsRepositoryPackage(rootElementPackage),
                        buildType()
                )
                .indent("    ")
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (FilerException e){
            e.printStackTrace();
        }
    }

    private TypeSpec buildType () {
        return TypeSpec.interfaceBuilder(getNotificationEmailsRepositoryName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(buildExtends())
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private ParameterizedTypeName buildExtends () {
        return ParameterizedTypeName.get(
                ClassName.get(JpaRepository.class),
                notificationEmailsClassName,
                ClassName.get(Long.class));
    }
}
