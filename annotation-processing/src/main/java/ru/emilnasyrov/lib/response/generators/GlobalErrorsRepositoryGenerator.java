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
import java.util.ArrayList;
import java.util.List;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.GlobalErrorsParams.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.buildJpaAllAnd;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.buildJpaAllOr;

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
                .addMethods(deleteMethods())
                .addMethods(findMethods())
                .addMethods(countMethods())
                .build();
    }

    private List<MethodSpec> deleteMethods () {
        List<MethodSpec> deleteMethods = new ArrayList<>();

        deleteMethods.add(buildJpaAllAnd("delete", void.class, code));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, importance));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, date));

        deleteMethods.add(buildJpaAllAnd("delete", void.class, code, importance));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, code, date));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, importance, date));

        deleteMethods.add(buildJpaAllOr("delete", void.class, code, importance));
        deleteMethods.add(buildJpaAllOr("delete", void.class, code, date));
        deleteMethods.add(buildJpaAllOr("delete", void.class, importance, date));

        deleteMethods.add(buildJpaAllAnd("delete", void.class, code, importance, date));
        deleteMethods.add(buildJpaAllOr("delete", void.class, code, importance, date));

        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, code));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, importance));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, date));

        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, code, importance));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, code, date));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, importance, date));

        deleteMethods.add(buildJpaAllOr("deleteAll", void.class, code, importance));
        deleteMethods.add(buildJpaAllOr("deleteAll", void.class, code, date));
        deleteMethods.add(buildJpaAllOr("deleteAll", void.class, importance, date));

        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, code, importance, date));
        deleteMethods.add(buildJpaAllOr("deleteAll", void.class, code, importance, date));
        return deleteMethods;
    }

    private List<MethodSpec> findMethods () {
        List<MethodSpec> findMethods = new ArrayList<>();

        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), code));
        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), importance));
        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), date));

        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), code, importance));
        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), code, date));
        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), importance, date));

        findMethods.add(buildJpaAllOr("findAll", buildFindReturns(), code, importance));
        findMethods.add(buildJpaAllOr("findAll", buildFindReturns(), code, date));
        findMethods.add(buildJpaAllOr("findAll", buildFindReturns(), importance, date));

        findMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), code, importance, date));
        findMethods.add(buildJpaAllOr("findAll", buildFindReturns(), code, importance, date));

        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, code));
        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, importance));
        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, date));

        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, code, importance));
        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, code, date));
        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, importance, date));

        findMethods.add(buildJpaAllOr("findFirst", globalErrorsClassName, code, importance));
        findMethods.add(buildJpaAllOr("findFirst", globalErrorsClassName, code, date));
        findMethods.add(buildJpaAllOr("findFirst", globalErrorsClassName, importance, date));

        findMethods.add(buildJpaAllAnd("findFirst", globalErrorsClassName, code, importance, date));
        findMethods.add(buildJpaAllOr("findFirst", globalErrorsClassName, code, importance, date));

        return findMethods;
    }

    private List<MethodSpec> countMethods () {
        List<MethodSpec> countMethods = new ArrayList<>();

        countMethods.add(buildJpaAllAnd("countAll", Long.class, code));
        countMethods.add(buildJpaAllAnd("countAll", Long.class, importance));
        countMethods.add(buildJpaAllAnd("countAll", Long.class, date));

        countMethods.add(buildJpaAllAnd("countAll", Long.class, code, importance));
        countMethods.add(buildJpaAllAnd("countAll", Long.class, code, date));
        countMethods.add(buildJpaAllAnd("countAll", Long.class, importance, date));

        countMethods.add(buildJpaAllOr("countAll", Long.class, code, importance));
        countMethods.add(buildJpaAllOr("countAll", Long.class, code, date));
        countMethods.add(buildJpaAllOr("countAll", Long.class, importance, date));

        countMethods.add(buildJpaAllAnd("countAll", Long.class, code, importance, date));
        countMethods.add(buildJpaAllOr("countAll", Long.class, code, importance, date));

        return countMethods;
    }

    private ParameterizedTypeName buildExtends () {
        return ParameterizedTypeName.get(
                ClassName.get(JpaRepository.class),
                globalErrorsClassName,
                ClassName.get(Long.class));
    }

    private ParameterizedTypeName buildFindReturns () {
        return ParameterizedTypeName.get(
                ClassName.get(List.class),
                globalErrorsClassName);
    }
}
