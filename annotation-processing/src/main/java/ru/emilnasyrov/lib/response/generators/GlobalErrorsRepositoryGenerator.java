package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class GlobalErrorsRepositoryGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    private final ClassName globalErrorsClassName;

    public GlobalErrorsRepositoryGenerator (Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.messager = messager;
        this.rootElement = rootElement;
        this.typeUtils = typeUtils;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();
        this.globalErrorsClassName = ClassName.get(getGlobalErrorsPackage(rootElementPackage), getGlobalErrorsName());
    }

    public void generate () throws Throwable {
        JavaFile javaFile = JavaFile
                .builder(
                        getGlobalErrorsRepositoryPackage(rootElementPackage),
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
        return TypeSpec.interfaceBuilder(getGlobalErrorsRepositoryName())
                .addSuperinterface(buildExtends())
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private ParameterizedTypeName buildExtends () {
        return ParameterizedTypeName.get(
                ClassName.get(JpaRepository.class),
                globalErrorsClassName,
                ClassName.get(Long.class));
    }
}
