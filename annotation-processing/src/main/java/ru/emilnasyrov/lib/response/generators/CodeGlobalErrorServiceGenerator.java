package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.emilnasyrov.lib.response.helper.FilesLocales;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.*;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class CodeGlobalErrorServiceGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    private final ClassName notificationEmailsRepositoryClassName;
    private final ClassName globalErrorsRepositoryClassName;
    private final ClassName smtpPropertiesClassName;
    private final ClassName notificationEmailsClassName;
    private final ClassName globalErrorsClassName;

    public CodeGlobalErrorServiceGenerator(Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement){
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.filer = filer;
        this.rootElement = rootElement;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();

        this.notificationEmailsRepositoryClassName = ClassName.get(getNotificationEmailsRepositoryPackage(rootElementPackage), getNotificationEmailsRepositoryName());
        this.globalErrorsRepositoryClassName = ClassName.get(getGlobalErrorsRepositoryPackage(rootElementPackage), getGlobalErrorsRepositoryName());
        this.smtpPropertiesClassName = ClassName.get(getSmtpPropertiesPackage(rootElementPackage), getSmtpPropertiesName());
        this.notificationEmailsClassName = ClassName.get(getNotificationEmailsPackage(rootElementPackage), getNotificationEmailsName());
        this.globalErrorsClassName = ClassName.get(getGlobalErrorsPackage(rootElementPackage), getGlobalErrorsName());
    }

    /**
     * Метод запуска генерации файла CodeGlobalErrorService
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    public void generate() throws Throwable {
        JavaFile.Builder builder = JavaFile
                .builder(
                        FilesLocales.getCodeGlobalErrorServicePackage(rootElementPackage),
                        buildType()
                )
                .indent("    ");

        JavaFile javaFile = builder.build();

        try {
            javaFile.writeTo(filer);
        } catch (FilerException e){
            e.printStackTrace();
        }
    }

    /**
     * Генерируем класс CodeGlobalErrorService
     *
     * @return TypeSpec класса
     */
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
                //TODO далее добавить методы класса
                .addMethods(buildAddNewGlobalErrorFunctions())
                .addMethod(buildSendHTMLMail())
                .addMethod(buildSendMessage())
                .addMethod(buildGetStackTrace())
                .addMethod(buildGetLocale())
                .build();
    }

    /**
     * Генерация метода getLocale получения размещения ошибки по StackTraceElement[]
     *
     * @return MethodSpec метода
     */
    private MethodSpec buildGetLocale () {
        return MethodSpec
                .methodBuilder("getLocale")
                .addModifiers(Modifier.PRIVATE)
                .returns(String.class)
                .addParameter(StackTraceElement[].class, "stackTrace")
                .addCode(getLocaleCode())
                .build();
    }

    /**
     * Генерация метода sendMessage отправки сообщения
     *
     * @return MethodSpec метод
     */
    private MethodSpec buildSendMessage () {
        return MethodSpec
                .methodBuilder("sendMessage")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addParameter(int.class, "importance")
                .addParameter(String.class, "message")
                .addParameter(String.class, "local")
                .addParameter(int.class, "code")
                .addParameter(String.class, "stackTrace")
                .addCode(sendMessageCode())
                .build();
    }

    /**
     * Метод генерации кода функции sendMessage
     *
     * @return CodeBlock блок кода
     */
    private CodeBlock sendMessageCode (){
        return CodeBlock
                .builder()
                .beginControlFlow("try")
                .beginControlFlow("if (importance == 1 || importance == 2)")
                .addStatement("$T<$T> notificationEmailsList = $N.findAll()", List.class, notificationEmailsClassName, addNotificationEmailsRepositoryField())
                .addStatement("$T subject = $N.getTitle() + \" Уровень ошибки: \" + importance", String.class, addSMTPProperties())
                .addStatement("$T body = \"Уровень ошибки: \" + importance + \". Сообщение об ошибке: \" + message + \". \\n\\n\\nОшибка произошла в \" + local", String.class)
                .beginControlFlow("for ($T notificationEmails : notificationEmailsList)", notificationEmailsClassName)
                .addStatement("$N(notificationEmails.getEmail(), subject, body)", buildSendHTMLMail())
                .endControlFlow()
                .endControlFlow()
                .nextControlFlow("catch (Exception e)")
                .addStatement("$T error = new $T(1, \"Не удалось отправить письмо. Сообщение: \" + e, \"/src/main/java/com.sk.webstudio.Transaction/modules/SecondaryFunctions ф-ия addNewGlobalErrorWithLocal\", null, 1)", globalErrorsClassName, globalErrorsClassName)
                .addStatement("$N.save(error)", addGlobalErrorsRepository())
                .nextControlFlow("finally")
                .addStatement("$T error = new $T(code, message, local, stackTrace, importance)", globalErrorsClassName, globalErrorsClassName)
                .addStatement("$N.save(error)", addGlobalErrorsRepository())
                .endControlFlow()
                .build();
    }

    /**
     * Генерация метода sendHTMLMail отправки сообщения
     *
     * @return
     */
    private MethodSpec buildSendHTMLMail () {
        return MethodSpec
                .methodBuilder("sendHTMLMail")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addException(MessagingException.class)
                .addParameter(String.class, "to")
                .addParameter(String.class, "subject")
                .addParameter(String.class, "html")
                .addCode(sendHTMLMailCode())
                .build();
    }

    /**
     * Генерация кода для метода sendHTMLMail отправки письма
     *
     * @return CodeBlock блок кода
     */
    private CodeBlock sendHTMLMailCode(){
        return CodeBlock
                .builder()
                .addStatement("$N = $N.createMimeMessage()", messageField(), mailSenderField())
                .addStatement("$T helper = new $T($N, true)", MimeMessageHelper.class, MimeMessageHelper.class, messageField())
                .addStatement("helper.setTo(to)")
                .addStatement("helper.setSubject(subject)")
                .addStatement("$N.setFrom(new $T($N.getUser()))", messageField(), InternetAddress.class, addSMTPProperties())
                .addStatement("$N.setContent(html, $N)", messageField(), messageTypeField())
                .beginControlFlow("$T task = () ->", Runnable.class)
                .addStatement("$N.send($N)", mailSenderField(), messageField())
                .endControlFlow()
                .addStatement("")
                .addStatement("$T thread = new $T(task)", Thread.class, Thread.class)
                .addStatement("thread.start()")
                .build();
    }

    /**
     * Генерация функций addNewGlobalError, отличающихся параметрами
     *
     * @return список MethodSpec функций addNewGlobalError
     */
    private List<MethodSpec> buildAddNewGlobalErrorFunctions() {
        List<MethodSpec> addNewGlobalError = new ArrayList<>();
        MethodSpec addNewGlobalErrorWithStackTrace = MethodSpec
                .methodBuilder("addNewGlobalError")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "code")
                .addParameter(String.class, "message")
                .addParameter(StackTraceElement[].class, "stackTrace")
                .addParameter(int.class, "importance")
                .returns(void.class)
                .addCode("$N(importance, message, $N(stackTrace), code, $N(stackTrace));", buildSendMessage(), buildGetLocale(), buildGetStackTrace())
                .build();

        MethodSpec addNewGlobalErrorWithLocal = MethodSpec
                .methodBuilder("addNewGlobalError")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "code")
                .addParameter(String.class, "message")
                .addParameter(String.class, "local")
                .addParameter(int.class, "importance")
                .returns(void.class)
                .addCode("$N(importance, message, local, code, null);", buildSendMessage())
                .build();

        addNewGlobalError.add(addNewGlobalErrorWithStackTrace);
        addNewGlobalError.add(addNewGlobalErrorWithLocal);
        return addNewGlobalError;
    }

    /**
     * Добавление не spring свойств класса
     *
     * @return список FieldSpec свойств класса
     */
    private List<FieldSpec> addSomeFields (){
        List<FieldSpec> someFields = new ArrayList<>();
        someFields.add(messageTypeField());
        someFields.add(mailSenderField());
        someFields.add(messageField());
        return someFields;
    }

    /**
     * Добавление spring свойств класса
     *
     * @return список FieldSpec свойств класса
     */
    private List<FieldSpec> addSpringFields () {
        List<FieldSpec> springFields = new ArrayList<>();
        springFields.add(addNotificationEmailsRepositoryField());
        springFields.add(addSMTPProperties());
        springFields.add(addGlobalErrorsRepository());
        return springFields;
    }

    /**
     * Генерация конструктора
     * Можно использовать для генерации конструкторов от любых переменных.
     * Если на вход будет подан ClassName класса ExceptionDateResponse, то будет сгенерирован конструктор, задающий
     * параметр ExceptionDateResponse exceptionDateResponse и присваивающий переменной класса this.exceptionDateResponse
     * значение параметра конструктора exceptionDateResponse
     *
     * @param classNames элементы, по которым сгенерировать параметры конструктора
     * @return MethodSpec конструктора
     */
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

    /**
     * Генерация кода настройки mailSender-а. Используется в конструкторе
     *
     * @return CodeBlock блок кода настройки
     */
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
                .addStatement("props.put(\"mail.debug\", $N.getDebug())", addSMTPProperties())
                .addStatement("props.put(\"mail.smtp.ssl.enable\", $N.getSsl())", addSMTPProperties())
                .addStatement("$N.setJavaMailProperties(props)", mailSenderField())
                .build();
    }

    /**
     * Код метода getLocale
     *
     * @return CodeBlock блок кода
     */
    private CodeBlock getLocaleCode () {
        return CodeBlock
                .builder()
                .addStatement("$T result = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("$T<$T> iterator = $T.stream(stackTrace).iterator()", Iterator.class, StackTraceElement.class, Arrays.class)
                .beginControlFlow("while (iterator.hasNext())")
                .addStatement("$T stackTraceElement = iterator.next()", StackTraceElement.class)
                .addStatement("$T[] subStr = stackTraceElement.getClassName().split(\"\\\\.\")", String.class)
                .addStatement("result.append(stackTraceElement.getClassName()).append(\" метод \").append(stackTraceElement.getMethodName()).append(\" строка \").append(stackTraceElement.getLineNumber()).append(\"\\n\")")
                .endControlFlow()
                .addStatement("return result.toString()")
                .build();
    }

    /**
     * Генерация метода getStackTrace, преобразующего массив StackTraceElement-ов в строку
     *
     * @return MethodSpec метода
     */
    private MethodSpec buildGetStackTrace () {
        return MethodSpec
                .methodBuilder("getStackTrace")
                .addModifiers(Modifier.PRIVATE)
                .returns(String.class)
                .addParameter(StackTraceElement[].class, "stackTrace")
                .addCode(getStackTraceCode())
                .build();
    }

    /**
     * Код метода getStackTrace
     *
     * @return CodeBlock блок кода метода getStackTrace
     */
    private CodeBlock getStackTraceCode () {
        return CodeBlock
                .builder()
                .addStatement("$T result = new $T()", StringBuilder.class, StringBuilder.class)
                .addStatement("$T<$T> iterator = $T.stream(stackTrace).iterator()", Iterator.class, StackTraceElement.class, Arrays.class)
                .beginControlFlow("while (iterator.hasNext())")
                .addStatement("result.append(iterator.next().toString()).append(\"\\n\")")
                .endControlFlow()
                .addStatement("return result.toString()")
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

    /**
     * Генерация переменной (свойства) MESSAGE_TYPE класса, содержащей след инфу:
     * text/html; charset=utf-8
     *
     * @return FieldSpec переменной
     */
    private FieldSpec messageTypeField() {
        return FieldSpec
                .builder(String.class, "MESSAGE_TYPE")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("\"text/html; charset=utf-8\"")
                .build();
    }

    /**
     * Генерация переменой (свойства) mailSender класса, объекта класса JavaMailSenderImpl
     *
     * @return FieldSpec переменной
     */
    private FieldSpec mailSenderField () {
        return FieldSpec
                .builder(JavaMailSenderImpl.class, "mailSender")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", JavaMailSenderImpl.class)
                .build();
    }

    /**
     * Генерация переменной (свойства) message класса, объекта класса MimeMessage
     *
     * @return FieldSpec переменной
     */
    private FieldSpec messageField () {
        return FieldSpec
                .builder(MimeMessage.class, "message")
                .addModifiers(Modifier.PRIVATE)
                .initializer("$N.createMimeMessage()", mailSenderField())
                .build();
    }

    /**
     * Генерация переменной (свойства) класса, объекта класса репозитория NotificationEmailsRepository
     * Генерация происходит с помощью ClassName, полученного зная пакет и имея класса
     *
     * @return FieldSpec переменной
     */
    private FieldSpec addNotificationEmailsRepositoryField () {
        return FieldSpec
                .builder(
                        notificationEmailsRepositoryClassName,
                        toLowerCaseFirstLetter(notificationEmailsRepositoryClassName.simpleName())
                )
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    /**
     * Генерация переменной (свойства) класса, объекта класса репозитория GlobalErrorsRepository
     * Генерация происходит с помощью ClassName, полученного зная пакет и имея класса
     *
     * @return FieldSpec переменной
     */
    private FieldSpec addGlobalErrorsRepository () {
        return FieldSpec
                .builder(
                        globalErrorsRepositoryClassName,
                        toLowerCaseFirstLetter(globalErrorsRepositoryClassName.simpleName())
                )
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    /**
     * Генерация переменной (свойства) класса, объекта класса SMTPProperties
     * Генерация происходит с помощью ClassName, полученного зная пакет и имея класса
     *
     * @return FieldSpec переменной
     */
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
