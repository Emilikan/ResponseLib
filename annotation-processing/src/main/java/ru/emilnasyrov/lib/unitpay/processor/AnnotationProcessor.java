package ru.emilnasyrov.lib.unitpay.processor;

import com.google.auto.service.AutoService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.annotates.GlobalError;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;
import ru.emilnasyrov.lib.unitpay.modules.AbstractException;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 1. Ищем классы и методы, которые аннотированы @OnlyForUnitpay
 * 2. Ищем классы и методы, в которых используются предыдущие классы и методы
 * 3. Проверяем, методы:
 *      1. Аннотирован ли сам метод? - Да - *все ок*
 *          - Нет - *п. 3.2*
 *      2. Аннотирован ли класс с этим методом? - Да - *все ок*
 *          - Нет - *выдаем ошибку*
 * 4. Проверяем классы:
 */

/**
 * Про TypeElement
 *
 * package com.example. ... // PackageElement
 *
 * public class Foo { // TypeElement
 *      private int a; // VariableElement
 *      private Foo f; // VariableElement
 *
 *      public Foo () {} // ExecutableElement
 *      public void setA ( // ExecutableElement
 *          int a // TypeElement
 *          ) {}
 * }
 */

/**
 * Element
 * item.getEnclosedElements() - получаем список элементами, находящимися внутри него (конструктор OnlyForUnitpayClass, поле a, метод getA())
 * item.getModifiers() - получаем модификатор элемента ([privat], [public]...)
 *
 * TypeElement
 * typeElement.getSuperclass() - получаем суперКласс (extends)
 * typeElement.getEnclosedElements() - получаем список с элементами, находящимися внутри него (конструктор OnlyForUnitpayClass, поле a, метод getA())
 * typeElement.getInterfaces() - список интерфейсов или пусто (implements)
 * typeElement.getNestingKind - тип вложенности элемента
 */


@SupportedAnnotationTypes({"ru.emilnasyrov.lib.unitpay.annotates.HttpException", "org.springframework.boot.autoconfigure.SpringBootApplication"})
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private final List<Element> annotatedOnlyForUnitpayClasses = new ArrayList<>();

    private final String packageName = "ru.emilnasyrov.lib.unitpay";
    private Element springRootElement = null;

    private boolean addGlobalErrorFiles = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeMirror abstractExceptionTypeMirror = elementUtils.getTypeElement(AbstractException.class.getPackageName() + "." + AbstractException.class.getSimpleName()).asType();
        TypeMirror runtimeExceptionTypeMirror = elementUtils.getTypeElement("java.lang.RuntimeException").asType();

        // STEP 1 выполняем проверку элементов и подсчитываем их количество
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HttpException.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                annotatedOnlyForUnitpayClasses.add(annotatedElement);
                if (annotatedElement.getAnnotation(HttpException.class).globalError().turnOn()){
                    // сообщаем программе, что пользователь использует глобальный обработчик ошибок
                    addGlobalErrorFiles = true;

                    // проверка, что класс, использующийся с обработчиком глобальных ошибок, расширяет класс AbstractException
                    TypeElement annotatedTypeElement = (TypeElement) annotatedElement;
                    if (!annotatedTypeElement.getSuperclass().equals(abstractExceptionTypeMirror)){
                        error(annotatedElement, "Elements using global error handler statements must extends from the class AbstractException");
                        return true;
                    }
                } else {
                    // проверка, что классы, которые не с GlobalError, наследуются от RuntimeException
                    TypeElement annotatedTypeElement = (TypeElement) annotatedElement;
                    if (!annotatedTypeElement.getSuperclass().equals(runtimeExceptionTypeMirror)){
                        error(annotatedElement, "The class %s must extend the RuntimeException class", annotatedElement.getSimpleName());
                        return true;
                    }
                }

                if (!annotatedElement.getModifiers().contains(Modifier.PUBLIC)){
                    error(annotatedElement, "The class %s must be public", annotatedElement.getSimpleName());
                    return true;
                }
            } else {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        HttpException.class.getSimpleName());
                return true;
            }
        }

        // плохая идея искать только SpringBootApplication, но т.к. библиотека в данный момент расчитана на проект,
        // который я сам писал или буду писать и не расчитана на open source, то приемлимо так оставить
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(SpringBootApplication.class)){
            if (springRootElement==null) springRootElement = annotatedElement;
            else {
                error(annotatedElement, "The project should only have 1 a class annotated @SpringBootApplication");
                return true;
            }
        }

        if (springRootElement==null){
            error(annotatedOnlyForUnitpayClasses.get(0), "The project must have a class annotated @SpringBootApplication");
            return true;
        }

        if (addGlobalErrorFiles){
            // нужна проверка наличия параметров настройки

            try {
                writeNotificationEmails();
                writeGlobalErrors();
                writeSMTPProperties();
                writeGlobalErrorsRepository();
                writeNotificationEmailsRepository();
                writeCodeGlobalErrorService();
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }

        if (annotatedOnlyForUnitpayClasses.size()!=0) {
            try {
                writeAwesomeExceptionHandler(annotatedOnlyForUnitpayClasses);
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }

        return true;
    }

    private void writeCodeGlobalErrorService() throws IOException {
        String mClassName = "CodeGlobalErrorService";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".service;");
            out.println();

            // импорты
            out.println("import " + springRootElement.getEnclosingElement() + ".entities.NotificationEmails;");
            out.println("import " + springRootElement.getEnclosingElement() + ".repositories.NotificationEmailsRepository;");
            out.println("import " + springRootElement.getEnclosingElement() + ".entities.GlobalErrors;");
            out.println("import " + springRootElement.getEnclosingElement() + ".repositories.GlobalErrorsRepository;");
            out.println("import org.springframework.mail.javamail.JavaMailSenderImpl;");
            out.println("import org.springframework.mail.javamail.MimeMessageHelper;");
            out.println("import org.springframework.stereotype.Service;");
            out.println("import javax.mail.MessagingException;");
            out.println("import javax.mail.internet.InternetAddress;");
            out.println("import javax.mail.internet.MimeMessage;");
            out.println("import java.util.Arrays;");
            out.println("import java.util.Iterator;");
            out.println("import java.util.List;");
            out.println("import java.util.Properties;");
            out.println("import " + springRootElement.getEnclosingElement() + ".properties.SMTPProperties;");
            out.println();
            // Объявление класса
            out.println("@Service");
            out.println("public class " + mClassName + " {");
            out.println("    private final NotificationEmailsRepository notificationEmailsRepository;");
            out.println("    private final GlobalErrorsRepository globalErrorsRepository;");
            out.println("    private final static String MESSAGE_TYPE = \"text/html; charset=utf-8\";");
            out.println("    private final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();");
            out.println("    private final SMTPProperties smtpProperties;");
            out.println("    private MimeMessage message = mailSender.createMimeMessage();");
            out.println();

            out.println("    public " + mClassName + "(NotificationEmailsRepository notificationEmailsRepository, GlobalErrorsRepository globalErrorsRepository, SMTPProperties smtpProperties){");
            out.println("        this.notificationEmailsRepository = notificationEmailsRepository;");
            out.println("        this.globalErrorsRepository = globalErrorsRepository;");
            out.println("        this.smtpProperties = smtpProperties;");
            out.println();
            out.println("        String host = smtpProperties.getHost();");
            out.println("        int port = smtpProperties.getPort();");
            out.println("        String user = smtpProperties.getUser();");
            out.println("        String password = smtpProperties.getPassword();");
            out.println();
            out.println("        mailSender.setHost(host);");
            out.println("        mailSender.setPort(port);");
            out.println("        mailSender.setUsername(user);");
            out.println("        mailSender.setPassword(password);");
            out.println();
            out.println("        Properties props = mailSender.getJavaMailProperties();");
            out.println("        props.put(\"mail.transport.protocol\", \"smtp\");");
            out.println("        props.put(\"mail.smtp.auth\", \"true\");");
            out.println("        props.put(\"mail.debug\", smtpProperties.getDebug());");
            out.println("        props.put(\"mail.smtp.ssl.enable\", smtpProperties.getSsl());");
            out.println("        mailSender.setJavaMailProperties(props);\n");
            out.println("    }");

            out.println("    /**");
            out.println("     * Добавляем новую глобальную ошибку");
            out.println("     *");
            out.println("     * @param code       код ошибки");
            out.println("     * @param message    текстовое описание");
            out.println("     * @param stackTrace стэктрейс ошибки");
            out.println("     * @param importance важность (от 1 до 4, где 1 - критическая ошибка, 2 - средняя важность, 3 - обычная важность, 4 - дебаг)");
            out.println("     */");
            out.println("    public void addNewGlobalError(int code, String message, StackTraceElement[] stackTrace, int importance) {");
            out.println("        sendMessage(importance, message, getLocale(stackTrace), code, getStackTrace(stackTrace));");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * Добавляем новую глобальную ошибку");
            out.println("     *");
            out.println("     * @param code       код ошибки");
            out.println("     * @param message    текстовое описание");
            out.println("     * @param local      путь до ошибки");
            out.println("     * @param importance важность (от 1 до 4, где 1 - критическая ошибка, 2 - средняя важность, 3 - обычная важность, 4 - дебаг)");
            out.println("     */");
            out.println("    public void addNewGlobalError(int code, String message, String local, int importance) {");
            out.println("        sendMessage(importance, message, local, code, null);");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * Отправка HTML письма");
            out.println("     *");
            out.println("     * @param to      Адресат");
            out.println("     * @param subject Тема письма");
            out.println("     * @param html    Тело письма");
            out.println("     * @throws MessagingException Ошибка в инициализации отправителя");
            out.println("     */");
            out.println("    private void sendHTMLMail(String to, String subject, String html) throws MessagingException {");
            out.println("           message = mailSender.createMimeMessage();");
            out.println("        MimeMessageHelper helper = new MimeMessageHelper(message, true);");
            out.println();
            out.println("        helper.setTo(to);");
            out.println("        helper.setSubject(subject);");
            out.println("        message.setFrom(new InternetAddress(smtpProperties.getUser()));");
            out.println("        message.setContent(html, MESSAGE_TYPE);");
            out.println();
            out.println("        Runnable task = () -> {");
            out.println("            mailSender.send(message);");
            out.println("        };");
            out.println("        Thread thread = new Thread(task);");
            out.println("        thread.start();");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * Отправка сообщения");
            out.println();
            out.println("     * @param importance важность (от 1 до 4, где 1 - критическая ошибка, 2 - средняя важность, 3 - обычная важность, 4 - дебаг)");
            out.println("     * @param message текст ошибки");
            out.println("     * @param local путь до места, где возникла ошибка");
            out.println("     * @param code код ошибки");
            out.println("     * @param stackTrace stackTrace ошибки");
            out.println("     */");
            out.println("    private void sendMessage (int importance, String message, String local, int code, String stackTrace) {");
            out.println("        try {");
            out.println("            if (importance == 1 || importance == 2) {");
            out.println("                List<NotificationEmails> notificationEmailsList = notificationEmailsRepository.findAll();");
            out.println("                String subject = smtpProperties.getTitle() + \" Уровень ошибки: \" + importance;");
            out.println("                String body = \"Уровень ошибки: \" + importance + \". Сообщение об ошибке: \" + message + \". \\n\\n\\nОшибка произошла в \" + local;");
            out.println("                for (NotificationEmails notificationEmails : notificationEmailsList) {");
            out.println("                    sendHTMLMail(notificationEmails.getEmail(), subject, body);");
            out.println("                }");
            out.println("            }");
            out.println("        } catch (Exception e) {");
            out.println("            GlobalErrors error = new GlobalErrors(");
            out.println("                    1,");
            out.println("                    \"Не удалось отправить письмо. Сообщение: \" + e,");
            out.println("                    \"/src/main/java/com.sk.webstudio.Transaction/modules/SecondaryFunctions ф-ия addNewGlobalErrorWithLocal\",");
            out.println("                    null,");
            out.println("                    1);");
            out.println("            globalErrorsRepository.save(error);");
            out.println("        } finally {");
            out.println("            GlobalErrors error = new GlobalErrors(code, message, local, stackTrace, importance);");
            out.println("            globalErrorsRepository.save(error);");
            out.println("        }");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * Получаем читабельный stackTrace, конвертированный в строку");
            out.println("     *");
            out.println("     * @param stackTrace stackTrace типа StackTraceElement[]");
            out.println("     * @return stackTrace, конвертированный в строку");
            out.println("     */");
            out.println("    private String getStackTrace (StackTraceElement[] stackTrace) {");
            out.println("        StringBuilder result = new StringBuilder();");
            out.println("        Iterator<StackTraceElement> iterator = Arrays.stream(stackTrace).iterator();");
            out.println("        while (iterator.hasNext()){");
            out.println("            result.append(iterator.next().toString()).append(\"\\n\");");
            out.println("        }");
            out.println("        return result.toString();");
            out.println("    }");
            out.println();

            out.println("    /**");
            out.println("     * получаем путь до ошибки из StackTraceElement[]");
            out.println("     *");
            out.println("     * @param stackTrace stackTrace типа StackTraceElement[]");
            out.println("     * @return путь до ошибки");
            out.println("     */");
            out.println("    private String getLocale (StackTraceElement[] stackTrace) {");
            out.println("        StringBuilder result = new StringBuilder();");
            out.println("        Iterator<StackTraceElement> iterator = Arrays.stream(stackTrace).iterator();");
            out.println("        while (iterator.hasNext()){");
            out.println("            StackTraceElement stackTraceElement = iterator.next();");
            out.println("            String[] subStr = stackTraceElement.getClassName().split(\"\\\\.\");");
            out.println("            result.append(stackTraceElement.getClassName()).append(\" метод \").append(stackTraceElement.getMethodName()).append(\" строка \").append(stackTraceElement.getLineNumber()).append(\"\\n\");");
            out.println("        }");
            out.println("        return result.toString();");
            out.println("    }");
            // конец класса
            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeAwesomeExceptionHandler(List<Element> annotatedClasses) throws IOException {
        String mClassName = "AwesomeExceptionHandler";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            //TODO заменить на путь от начального класса Spring (по аннотации)
            out.println("package " + springRootElement.getEnclosingElement() + ".handler;");
            out.println();

            // импорты
            out.println("import " + packageName + ".modules.ExceptionDateResponse;");
            out.println("import org.springframework.web.bind.annotation.ControllerAdvice;");
            out.println("import org.springframework.http.ResponseEntity;");
            out.println("import org.springframework.http.HttpStatus;");
            out.println("import "+packageName+".modules.Locals;");
            out.println("import org.springframework.web.bind.annotation.ExceptionHandler;");
            out.println("import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;");
            // импорт класс глобальных ошибок
            if(addGlobalErrorFiles){
                out.println("import " + springRootElement.getEnclosingElement() + ".service.CodeGlobalErrorService;");
            }
            // импорты аннотированных классов
            for(Element element: annotatedClasses){
                out.println("import " + element.getEnclosingElement() + "." + element.getSimpleName() + ";");

                // импорт сторонних ответов
                TypeElement typeElement=null;
                TypeElement defaultTypeElement=null;
                try {
                    Class<?> mClass = element.getAnnotation(HttpException.class).responseClass();
                } catch (MirroredTypeException mte){
                    typeElement = asTypeElement(mte.getTypeMirror());
                }

                try {
                    defaultTypeElement = elementUtils.getTypeElement("ru.emilnasyrov.lib.unitpay.modules.ExceptionDateResponse");
                } catch (MirroredTypeException mte){
                    defaultTypeElement = asTypeElement(mte.getTypeMirror());
                }

                //out.println("" + typeElement);
                if (typeElement!=defaultTypeElement){
                    out.println("import " + typeElement + ";");
                }
            }
            out.println();

            // начало класса
            out.println("@ControllerAdvice");
            out.print("public class ");
            out.print(mClassName);
            out.println(" extends ResponseEntityExceptionHandler {");

            if (addGlobalErrorFiles){
                out.println("    private final CodeGlobalErrorService globalErrorService;");

                out.println("    public "+mClassName+"(CodeGlobalErrorService globalErrorService){");
                out.println("        this.globalErrorService = globalErrorService;");
                out.println("    }");
            }

            // хендлеры
            for (Element element: annotatedClasses){
                String exceptionClass = element.getSimpleName().toString();
                TypeElement typeElement=null;

                try {
                    Class<?> mClass = element.getAnnotation(HttpException.class).responseClass();
                } catch (MirroredTypeException mte){
                    typeElement = asTypeElement(mte.getTypeMirror());
                }

                String exceptionResponse = typeElement.getSimpleName().toString();
                int code = element.getAnnotation(HttpException.class).code();
                String message = element.getAnnotation(HttpException.class).message();
                Locals local = element.getAnnotation(HttpException.class).local();
                HttpStatus httpStatus = element.getAnnotation(HttpException.class).status();

                out.println();
                out.print("    @ExceptionHandler(");
                out.print(exceptionClass);
                out.println(".class)");

                if (!addGlobalErrorFiles) {
                    out.println("    private ResponseEntity<?> handle" + exceptionClass + "() {");
                } else {
                    out.println("    private ResponseEntity<?> handle" + exceptionClass + "("+exceptionClass+" e) {");
                }

//                out.println("       return generateError"+exceptionResponse+"(");
//                out.println("               HttpStatus."+httpStatus.getReasonPhrase().toUpperCase().replace(" ", "_")+",");
//                out.println("               "+code+",");
//                out.println("               \""+message+"\",");
//                out.println("               Locals."+local+");");

                // добавление вызова глобальной ошибки в код
                GlobalError globalError = element.getAnnotation(HttpException.class).globalError();
                if(globalError.turnOn()){
                    out.println("       globalErrorService.addNewGlobalError(");
                    out.println("                "+code+",");
                    out.println("                \""+globalError.message()+". Сообщение об ошибке: \" + e.getMMessage(),");
                    out.println("                e.getStackTraceElements(),");
                    out.println("                "+globalError.importance()+"");
                    out.println("       );");
                }

                // TODO класс ответа обязательно должен реализовывать пустой конструктор (написать проверку на это)
                out.println("       return new " + exceptionResponse + "().generateError(");
                out.println("               HttpStatus."+httpStatus.getReasonPhrase().toUpperCase().replace(" ", "_")+",");
                out.println("               "+code+",");
                out.println("               \""+message+"\",");
                out.println("               Locals."+local);
                out.println("               );");

                out.println("    }");
            }

            // конец класса
            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSMTPProperties() throws IOException{
        String mClassName = "SMTPProperties";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".properties;");
            out.println();

            // импорты
            out.println("import org.springframework.boot.context.properties.ConfigurationProperties;");
            out.println("import org.springframework.context.annotation.Configuration;");
            out.println();

            out.println("@Configuration(\"SMTPProperties\")");
            out.println("@ConfigurationProperties(\"response.lib.smtp\")");
            out.println("public class " + mClassName + "{");
            out.println("    private String host;");
            out.println("    private int port;");
            out.println("    private String user;");
            out.println("    private String password;");
            out.println("    private String title;");
            out.println("    private boolean ssl;");
            out.println("    private boolean debug;");
            out.println();

            out.println("    public String getHost(){");
            out.println("        return this.host;");
            out.println("    }");

            out.println("    public int getPort(){");
            out.println("        return this.port;");
            out.println("    }");

            out.println("    public String getUser(){");
            out.println("        return this.user;");
            out.println("    }");

            out.println("    public String getTitle(){");
            out.println("        return this.title;");
            out.println("    }");

            out.println("    public String getPassword(){");
            out.println("        return this.password;");
            out.println("    }");

            out.println("    public boolean getSsl(){");
            out.println("        return this.ssl;");
            out.println("    }");

            out.println("    public boolean getDebug(){");
            out.println("        return this.debug;");
            out.println("    }");

            out.println("    public void setHost(String host){");
            out.println("        this.host = host;");
            out.println("    }");

            out.println("    public void setTitle(String title){");
            out.println("        this.title = title;");
            out.println("    }");

            out.println("    public void setPort(int port){");
            out.println("        this.port = port;");
            out.println("    }");

            out.println("    public void setUser(String user){");
            out.println("        this.user = user;");
            out.println("    }");

            out.println("    public void setPassword(String password){");
            out.println("        this.password = password;");
            out.println("    }");

            out.println("    public void setSsl(boolean ssl){");
            out.println("        this.ssl = ssl;");
            out.println("    }");

            out.println("    public void setDebug(boolean debug){");
            out.println("        this.debug = debug;");
            out.println("    }");

            out.println("}");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // печатаем entities
    private void writeNotificationEmails () throws IOException {
        String mClassName = "NotificationEmails";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".entities;");
            out.println();

            // импорты
            out.println("import org.apache.commons.validator.routines.EmailValidator;");
            out.println("import javax.persistence.*;");
            out.println();

            out.println("@Entity");
            out.println("@Table(name = \"notification_emails\")");
            out.println("public class " + mClassName + "{");
            out.println("    @Id");
            out.println("    @GeneratedValue(strategy = GenerationType.IDENTITY)");
            out.println("    @Column(name = \"id\")");
            out.println("    private Long id;");
            out.println();

            out.println("    @Column(name = \"email\")");
            out.println("    private String email;");
            out.println();

            out.println("    @Column(name = \"name\")");
            out.println("    private String name;");
            out.println();

            out.println("    public NotificationEmails(){ }");
            out.println();

            out.println("    public NotificationEmails(String email, String name){");
            out.println("        if (EmailValidator.getInstance().isValid(email)){");
            out.println("            this.email = email;");
            out.println("            this.name = name;");
            out.println("        }");
            out.println("    }");

            out.println("    public Long getId(){");
            out.println("        return this.id;");
            out.println("    }");

            out.println("    public String getEmail(){");
            out.println("        return this.email;");
            out.println("    }");

            out.println("    public String getName(){");
            out.println("        return this.name;");
            out.println("    }");

            out.println("    public void setId(Long id){");
            out.println("        this.id = id;");
            out.println("    }");

            out.println("    public void setEmail(String email){");
            out.println("        this.email = email;");
            out.println("    }");

            out.println("    public void setName(String name){");
            out.println("        this.name = name;");
            out.println("    }");

            out.println("}");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // печатаем entities
    private void writeGlobalErrors () throws IOException {
        String mClassName = "GlobalErrors";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".entities;");
            out.println();

            // импорты
            out.println("import java.util.Date;");
            out.println("import javax.persistence.*;");
            out.println();

            out.println("@Entity");
            out.println("@Table(name = \"global_errors\")");
            out.println("public class " + mClassName + "{");
            out.println("    @Id");
            out.println("    @GeneratedValue(strategy = GenerationType.IDENTITY)");
            out.println("    @Column(name = \"id\")");
            out.println("    private Long id;");
            out.println();

            out.println("    @Column(name = \"code\", nullable = false)");
            out.println("    private int code;");
            out.println();

            out.println("    @Column(name = \"message\", nullable = false, columnDefinition = \"text\")");
            out.println("    private String message;");
            out.println();

            out.println("    @Column(name = \"location\", nullable = false, columnDefinition = \"text\")");
            out.println("    private String location;");
            out.println();

            out.println("    @Column(name = \"stack_trace\", columnDefinition = \"text\")");
            out.println("    private String stackTrace;");
            out.println();

            out.println("    @Column(name = \"importance\", nullable = false)");
            out.println("    private int importance;");
            out.println();

            out.println("    @Column(name = \"date\")");
            out.println("    private Date date;");
            out.println();

            out.println("    public GlobalErrors(){ }");
            out.println();

            out.println("    public GlobalErrors(int code, String message, String location, String stackTrace, int importance){");
            out.println("        this.code = code;");
            out.println("        this.message = message;");
            out.println("        this.location = location;");
            out.println("        this.importance = importance;");
            out.println("        this.date = new Date();");
            out.println("        this.stackTrace = stackTrace;");
            out.println("    }");

            out.println("    public Long getId(){");
            out.println("        return this.id;");
            out.println("    }");

            out.println("    public int getCode(){");
            out.println("        return this.code;");
            out.println("    }");

            out.println("    public String getMessage(){");
            out.println("        return this.message;");
            out.println("    }");

            out.println("    public String getLocation(){");
            out.println("        return this.location;");
            out.println("    }");

            out.println("    public String getStackTrace(){");
            out.println("        return this.stackTrace;");
            out.println("    }");

            out.println("    public int getImportance(){");
            out.println("        return this.importance;");
            out.println("    }");

            out.println("    public Date getDate(){");
            out.println("        return this.date;");
            out.println("    }");

            out.println("    public void setId(Long id){");
            out.println("        this.id = id;");
            out.println("    }");

            out.println("    public void setCode(int code){");
            out.println("        this.code = code;");
            out.println("    }");

            out.println("    public void setMessage(String message){");
            out.println("        this.message = message;");
            out.println("    }");

            out.println("    public void setLocation(String location){");
            out.println("        this.location = location;");
            out.println("    }");

            out.println("    public void setStackTrace(String stackTrace){");
            out.println("        this.stackTrace = stackTrace;");
            out.println("    }");

            out.println("    public void setImportance(int importance){");
            out.println("        this.importance = importance;");
            out.println("    }");

            out.println("    public void setDate(Date date){");
            out.println("        this.date = date;");
            out.println("    }");

            out.println("}");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // печатаем repository
    private void writeGlobalErrorsRepository() throws IOException {
        String mClassName = "GlobalErrorsRepository";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".repositories;");
            out.println();

            // импорты
            out.println("import "+springRootElement.getEnclosingElement()+".entities.GlobalErrors;");
            out.println("import org.springframework.data.jpa.repository.JpaRepository;");
            out.println("import org.springframework.stereotype.Repository;");
            out.println();

            out.println("@Repository");
            out.println("public interface " + mClassName + " extends JpaRepository<GlobalErrors, Long>{}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // печатаем repository
    private void writeNotificationEmailsRepository() throws IOException {
        String mClassName = "NotificationEmailsRepository";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            out.println("package " + springRootElement.getEnclosingElement() + ".repositories;");
            out.println();

            // импорты
            out.println("import "+springRootElement.getEnclosingElement()+".entities.NotificationEmails;");
            out.println("import org.springframework.data.jpa.repository.JpaRepository;");
            out.println("import org.springframework.stereotype.Repository;");
            out.println();

            out.println("@Repository");
            out.println("public interface " + mClassName + " extends JpaRepository<NotificationEmails, Long>{}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement)TypeUtils.asElement(typeMirror);
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
