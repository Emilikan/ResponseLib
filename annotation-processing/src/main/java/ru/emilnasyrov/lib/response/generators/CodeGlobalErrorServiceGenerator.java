package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import ru.emilnasyrov.lib.response.helper.FilesLocales;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.mail.internet.MimeMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class CodeGlobalErrorServiceGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    private JavaFile javaFile = null;

    private final ClassName notificationEmailsRepositoryClassName;
    private final ClassName globalErrorsRepositoryClassName;
    private final ClassName smtpPropertiesClassName;

    public CodeGlobalErrorServiceGenerator(Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement){
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.filer = filer;
        this.rootElement = rootElement;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();

        this.notificationEmailsRepositoryClassName = ClassName.get(getNotificationEmailsPackage(rootElementPackage), getNotificationEmailsName());
        this.globalErrorsRepositoryClassName = ClassName.get(getGlobalErrorsRepositoryPackage(rootElementPackage), getGlobalErrorsRepositoryName());
        this.smtpPropertiesClassName = ClassName.get(getSmtpPropertiesPackage(rootElementPackage), getSmtpPropertiesName());
    }

    public JavaFile generate() throws Throwable {
        JavaFile.Builder builder = JavaFile
                .builder(
                        FilesLocales.getCodeGlobalErrorServicePackage(rootElementPackage),
                        buildType()
                )
                .indent("    ");

        javaFile = builder.build();

        try {
            javaFile.writeTo(filer);
        } catch (FilerException e){
            e.printStackTrace();
        }
        return javaFile;
    }

    private TypeSpec buildType (){
        return TypeSpec.classBuilder(FilesLocales.getCodeGlobalErrorServiceName())
                .addAnnotation(Service.class)
                .addModifiers(Modifier.PUBLIC)
                .addFields(addSpringFields())
                .addFields(addSomeFields())
                .addMethod(
                        buildConstructor(
                                notificationEmailsRepositoryClassName,
                                globalErrorsRepositoryClassName,
                                smtpPropertiesClassName
                        )
                )
                .build();
    }

    private List<FieldSpec> addSomeFields (){
        List<FieldSpec> someFields = new ArrayList<>();
        someFields.add(messageTypeField());
        someFields.add(mailSenderField());
        someFields.add(messageField());
        return someFields;
    }

    private List<FieldSpec> addSpringFields () {
        List<FieldSpec> springFields = new ArrayList<>();
        springFields.add(addNotificationEmailsRepositoryField());
        springFields.add(addSMTPProperties());
        springFields.add(addGlobalErrorsRepository());
        return springFields;
    }

    private MethodSpec buildConstructor(ClassName... classNames) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(
                        addParameters(classNames)
                );

        for (ClassName className : classNames) {
            builder.addStatement("this.$L = $L", toLowerCaseFirstLetter(className.simpleName()), toLowerCaseFirstLetter(className.simpleName()));
        }

        // настройка почты
        builder.addCode(customizationMailSender());

        return builder.build();
    }

    private CodeBlock customizationMailSender () {
        return CodeBlock
                .builder()
                .addStatement("$N.setHost($N.getHost())", mailSenderField(), addSMTPProperties())
                .addStatement("$N.setPort($N.getPort())", mailSenderField(), addSMTPProperties())
                .addStatement("$N.setUsername($N.getUser())", mailSenderField(), addSMTPProperties())
                .addStatement("$N.setPassword($N.getPassword())", mailSenderField(), addSMTPProperties())
                .addStatement("$T props = $N.getJavaMailProperties()", Properties.class, mailSenderField())
                .addStatement("props.put(\"mail.transport.protocol\", \"smtp\")")
                .addStatement("props.put(\"mail.smtp.auth\", \"true\")")
                .addStatement("props.put(\"mail.debug\", this.$N.getDebug())", addSMTPProperties())
                .addStatement("props.put(\"mail.smtp.ssl.enable\", this.$N.getSsl())", addSMTPProperties())
                .addStatement("$N.setJavaMailProperties(props)", mailSenderField())
                .build();
    }

    /**
     * Добавляем список параметров по ClassName классов
     * Если на вход будет подан TypeElement класса ExceptionDateResponse, то будет сгенерирован параметр
     * ExceptionDateResponse exceptionDateResponse
     *
     * @param classNames ClassName классов параметров
     * @return список List параметров, сгенерированных по списку TypeElement
     */
    private List<ParameterSpec> addParameters(ClassName... classNames) {
        List<ParameterSpec> parameters = new ArrayList<>();
        for (ClassName className : classNames) {
            parameters.add(addParameter(className));
        }
        return parameters;
    }

    /**
     * Задаем (генерируем) параметр по ClassName класса, описывающего параметр
     * Если на вход будет подан TypeElement класса ExceptionDateResponse, то будет сгенерирован параметр
     * ExceptionDateResponse exceptionDateResponse
     *
     * @param className класс, описывающий тип параметра, в виде ClassName
     * @return ParameterSpec параметра в виде SomeClass someClass
     */
    private ParameterSpec addParameter(ClassName className) {
        return ParameterSpec
                .builder(
                        className,
                        toLowerCaseFirstLetter(className.simpleName())
                )
                .build();
    }

    private FieldSpec messageTypeField() {
        return FieldSpec
                .builder(String.class, "MESSAGE_TYPE")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("\"text/html; charset=utf-8\"")
                .build();
    }

    private FieldSpec mailSenderField () {
        return FieldSpec
                .builder(JavaMailSenderImpl.class, "mailSender")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", JavaMailSenderImpl.class)
                .build();
    }

    private FieldSpec messageField () {
        return FieldSpec
                .builder(MimeMessage.class, "message")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$N.createMimeMessage()", mailSenderField())
                .build();
    }

    private FieldSpec addNotificationEmailsRepositoryField () {
        return FieldSpec
                .builder(
                        notificationEmailsRepositoryClassName,
                        toLowerCaseFirstLetter(notificationEmailsRepositoryClassName.simpleName())
                )
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private FieldSpec addGlobalErrorsRepository () {
        return FieldSpec
                .builder(
                        globalErrorsRepositoryClassName,
                        toLowerCaseFirstLetter(globalErrorsRepositoryClassName.simpleName())
                )
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private FieldSpec addSMTPProperties () {
        return FieldSpec
                .builder(
                        smtpPropertiesClassName,
                        toLowerCaseFirstLetter(smtpPropertiesClassName.simpleName())
                )
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }


}
