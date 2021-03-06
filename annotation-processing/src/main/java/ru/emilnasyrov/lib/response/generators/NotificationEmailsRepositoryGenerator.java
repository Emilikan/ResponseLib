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
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.buildJpaAllAnd;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.buildJpaAllOr;
import static ru.emilnasyrov.lib.response.helper.NotificationEmailsParams.email;
import static ru.emilnasyrov.lib.response.helper.NotificationEmailsParams.name;

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
                .addMethods(deleteMethods())
                .addMethods(findMethods())
                .addMethods(countMethods())
                .build();
    }

    private List<MethodSpec> deleteMethods () {
        List<MethodSpec> deleteMethods = new ArrayList<>();

        deleteMethods.add(buildJpaAllAnd("delete", void.class, name));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, name, email));
        deleteMethods.add(buildJpaAllOr("delete", void.class, name, email));
        deleteMethods.add(buildJpaAllAnd("delete", void.class, email));

        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, name));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, name, email));
        deleteMethods.add(buildJpaAllOr("deleteAll", void.class, name, email));
        deleteMethods.add(buildJpaAllAnd("deleteAll", void.class, email));

        return deleteMethods;
    }

    private List<MethodSpec> findMethods () {
        List<MethodSpec> deleteMethods = new ArrayList<>();

        deleteMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), name));
        deleteMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), name, email));
        deleteMethods.add(buildJpaAllOr("findAll", buildFindReturns(), name, email));
        deleteMethods.add(buildJpaAllAnd("findAll", buildFindReturns(), email));

        deleteMethods.add(buildJpaAllAnd("findFirst", notificationEmailsClassName, name));
        deleteMethods.add(buildJpaAllAnd("findFirst", notificationEmailsClassName, name, email));
        deleteMethods.add(buildJpaAllOr("findFirst", notificationEmailsClassName, name, email));
        deleteMethods.add(buildJpaAllAnd("findFirst", notificationEmailsClassName, email));

        return deleteMethods;
    }

    private List<MethodSpec> countMethods () {
        List<MethodSpec> deleteMethods = new ArrayList<>();

        deleteMethods.add(buildJpaAllAnd("countAll", Long.class, name));
        deleteMethods.add(buildJpaAllAnd("countAll", Long.class, name, email));
        deleteMethods.add(buildJpaAllOr("countAll", Long.class, name, email));
        deleteMethods.add(buildJpaAllAnd("countAll", Long.class, email));

        return deleteMethods;
    }

    private ParameterizedTypeName buildExtends () {
        return ParameterizedTypeName.get(
                ClassName.get(JpaRepository.class),
                notificationEmailsClassName,
                ClassName.get(Long.class));
    }

    private ParameterizedTypeName buildFindReturns () {
        return ParameterizedTypeName.get(
                ClassName.get(List.class),
                notificationEmailsClassName);
    }
}
