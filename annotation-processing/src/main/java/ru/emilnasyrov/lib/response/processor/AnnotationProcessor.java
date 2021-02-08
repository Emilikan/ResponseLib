package ru.emilnasyrov.lib.response.processor;

import com.google.auto.service.AutoService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.emilnasyrov.lib.response.annotates.HttpException;
import ru.emilnasyrov.lib.response.generators.*;
import ru.emilnasyrov.lib.response.modules.AbstractException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Set;

import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"ru.emilnasyrov.lib.unitpay.annotates.HttpException", "org.springframework.boot.autoconfigure.SpringBootApplication"})
public class AnnotationProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    private final ArrayList<Element> annotatedClasses = new ArrayList<>();
    private Element springRootElement = null;

    private boolean addGlobalErrorFiles = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeMirror abstractExceptionTypeMirror = elementUtils.getTypeElement(AbstractException.class.getPackageName() + "." + AbstractException.class.getSimpleName()).asType();
        TypeMirror runtimeExceptionTypeMirror = elementUtils.getTypeElement("java.lang.RuntimeException").asType();

        // STEP 1 выполняем проверку элементов и подсчитываем их количество
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HttpException.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                annotatedClasses.add(annotatedElement);
                if (annotatedElement.getAnnotation(HttpException.class).globalError().turnOn()){
                    // сообщаем программе, что пользователь использует глобальный обработчик ошибок
                    addGlobalErrorFiles = true;

                    // проверка, что класс, использующийся с обработчиком глобальных ошибок, расширяет класс AbstractException
                    TypeElement annotatedTypeElement = (TypeElement) annotatedElement;
                    if (!annotatedTypeElement.getSuperclass().equals(abstractExceptionTypeMirror)){
                        error(annotatedElement, messager, "Elements using global error handler statements must extends from the class AbstractException");
                        return true;
                    }
                } else {
                    // проверка, что классы, которые не с GlobalError, наследуются от RuntimeException
                    TypeElement annotatedTypeElement = (TypeElement) annotatedElement;
                    if (!annotatedTypeElement.getSuperclass().equals(runtimeExceptionTypeMirror)){
                        error(annotatedElement, messager, "The class %s must extend the RuntimeException class", annotatedElement.getSimpleName());
                        return true;
                    }
                }

                if (!annotatedElement.getModifiers().contains(Modifier.PUBLIC)){
                    error(annotatedElement, messager, "The class %s must be public", annotatedElement.getSimpleName());
                    return true;
                }
            } else {
                error(annotatedElement, messager, "Only classes can be annotated with @%s",
                        HttpException.class.getSimpleName());
                return true;
            }
        }

        // плохая идея искать только SpringBootApplication, но т.к. библиотека в данный момент расчитана на проект,
        // который я сам писал или буду писать и не расчитана на open source, то приемлимо так оставить
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(SpringBootApplication.class)){
            if (springRootElement==null) springRootElement = annotatedElement;
            else {
                error(annotatedElement, messager, "The project should only have 1 a class annotated @SpringBootApplication");
                return true;
            }
        }

        if (springRootElement==null){
            error(annotatedClasses.get(0), messager, "The project must have a class annotated @SpringBootApplication");
            return true;
        }

        if (addGlobalErrorFiles){
            // нужна проверка наличия параметров настройки

            try {
                writeNotificationEmails();
            } catch (Throwable e) {
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class NotificationEmailsGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }

            try {
                writeGlobalErrors();
            } catch (Throwable e) {
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class GlobalErrorsGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }

            try {
                writeSMTPProperties();
            } catch (Throwable e) {
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class SMTPPropertiesGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }

            try {
                writeGlobalErrorsRepository();
            } catch (Throwable e) {
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class GlobalErrorsRepositoryGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }

            try {
                writeNotificationEmailsRepository();
            } catch (Throwable e) {
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class NotificationEmailsRepositoryGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }

            try {
                writeCodeGlobalErrorService();
            } catch (Throwable e){
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class CodeGlobalErrorServiceGenerator. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }
        }

        if (annotatedClasses.size()!=0) {
            try {
                writeAwesomeExceptionHandler();
            } catch (Throwable e){
                e.printStackTrace();
                error(springRootElement, messager, "Error while generate class AwesomeException. Message: %s. ErrorName: %s.", e.getMessage(), e.getClass().getSimpleName());
                return true;
            }
        }

        return true;
    }

    /**
     * Генерируем класс AwesomeExceptionHandler
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeAwesomeExceptionHandler() throws Throwable {
        new AwesomeExceptionHandlerGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement
        ).generate(annotatedClasses, addGlobalErrorFiles);
    }

    /**
     * Генерируем класс CodeGlobalErrorService
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeCodeGlobalErrorService() throws Throwable {
        new CodeGlobalErrorServiceGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    /**
     * Генерируем класс SMTPProperties
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeSMTPProperties() throws Throwable {
        new SMTPPropertiesGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    /**
     * Генерируем класс NotificationEmails, описывающий таблицу notification_emails
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeNotificationEmails () throws Throwable {
        new NotificationEmailsGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    /**
     * Генерируем класс GlobalErrors, описывающий таблицу global_errors
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeGlobalErrors () throws Throwable {
        new GlobalErrorsGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    /**
     * Генерируем репозиторий GlobalErrorsRepository
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeGlobalErrorsRepository() throws Throwable {
        new GlobalErrorsRepositoryGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    /**
     * Генерируем репозиторий NotificationEmailsRepository
     *
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    private void writeNotificationEmailsRepository() throws Throwable {
        new NotificationEmailsRepositoryGenerator(
                typeUtils,
                elementUtils,
                messager,
                filer,
                springRootElement).generate();
    }

    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
