package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.emilnasyrov.lib.response.annotates.HttpException;
import ru.emilnasyrov.lib.response.helper.FilesLocales;

import javax.annotation.Nullable;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class AwesomeExceptionHandlerGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;
    private final ClassName codeGlobalErrorClassName;

    public AwesomeExceptionHandlerGenerator(Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.messager = messager;
        this.filer = filer;
        this.rootElement = rootElement;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();
        this.codeGlobalErrorClassName = ClassName.get(FilesLocales.getCodeGlobalErrorServicePackage(rootElementPackage), FilesLocales.getCodeGlobalErrorServiceName());
    }

    /**
     * функция генерации файла AwesomeExceptionHandler
     *
     * @param exceptions список классов-ошибок
     * @param addGlobalErrorFiles используются ли в проекте классы-ошибки, использующие обработчик глобальных ошибок
     * @throws Throwable если что-то пошло не так (не так пойти может что угодно. Начиная от того, что нет места и заканчивая какой-либо ошибко библиотеки JavaPoet)
     */
    public void generate(ArrayList<Element> exceptions, boolean addGlobalErrorFiles) throws Throwable {
        JavaFile.Builder javaFileBuilder = JavaFile
                .builder(
                        FilesLocales.getAwesomeExceptionHandlerPackage(rootElementPackage),
                        buildType(exceptions, addGlobalErrorFiles)
                )
                .indent("    ");
        for (Element exception : exceptions){
            javaFileBuilder.addStaticImport(exception.getAnnotation(HttpException.class).status());
            javaFileBuilder.addStaticImport(exception.getAnnotation(HttpException.class).local());
        }

        JavaFile javaFile = javaFileBuilder.build();

        try {
            javaFile.writeTo(filer);
        } catch (FilerException e){
            e.printStackTrace();
        }

    }

    /**
     * Задаем (генерируем) класс AwesomeExceptionHandler
     *
     * @param exceptions список классов-ошибок
     * @param addGlobalErrorFiles используются ли в проекте классы-ошибки, использующие обработчик глобальных ошибок
     * @return TypeSpec - класс библиотеки JavaPoet, описывающий класс AwesomeExceptionHandler
     */
    private TypeSpec buildType(ArrayList<Element> exceptions, boolean addGlobalErrorFiles) {
        TypeSpec.Builder builder =  TypeSpec.classBuilder(FilesLocales.getAwesomeExceptionHandlerName())
                .addAnnotation(ControllerAdvice.class)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ResponseEntityExceptionHandler.class);

        if (addGlobalErrorFiles) {
            builder
                    .addField(addFieldFromClassName(codeGlobalErrorClassName))
                    .addMethod(buildConstructor(codeGlobalErrorClassName));
        }
        builder.addMethods(buildHandlers(exceptions));

        return builder.build();
    }

    /**
     * Добавляем переменную по ClassName класса, описывающего тип данных. Нужна для использования обработчика глобальных ошибок
     * Можно использовать с еще не сгенерированными классами (используется для генерации переменной типа
     * CodeGlobalErrorService, который генерируется на этом же этапе компиляции)
     *
     * Будет сгенерирована переменная типа
     * private final CodeGlobalErrorService codeGlobalErrorService;
     *
     * @param className ClassName класса, описывающего переменную
     *
     * @return FieldSpec переменной
     */
    private FieldSpec addFieldFromClassName(ClassName className) {
        return FieldSpec
                .builder(
                        className,
                        toLowerCaseFirstLetter(className.simpleName()))
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    /**
     * Добавляем конструктор в класс. Нужен для того, чтобы сконнектить bean для переменной codeGlobalErrorService
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
        return builder.build();
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
     * Генерируем методы-хендлеры в классе AwesomeExceptionHandler для каждого класса-исключения
     *
     * @param exceptions список исключений, для которых необходимо сгенерировать соответствующий методы-хендлеры
     * @return список MethodSpec методов-хендлеров
     */
    private List<MethodSpec> buildHandlers(ArrayList<Element> exceptions) {
        List<MethodSpec> handlers = new ArrayList<>();
        exceptions.forEach(exception -> handlers.add(buildHandler(exception)));
        return handlers;
    }

    /**
     * Генерация метода-хендлера для конкретного исключения
     *
     * @param exception исключение, для которого необходимо сгенерировать метод-хендлер
     * @return MethodSpec метода-хендлера
     */
    private MethodSpec buildHandler(Element exception) {
        String name = "handle" + exception.getSimpleName();
        HttpException httpExceptionAnnotation = exception.getAnnotation(HttpException.class);
        String httpStatus = httpExceptionAnnotation
                .status()
                .getReasonPhrase()
                .toUpperCase()
                .replace(" ", "_");
        int code = httpExceptionAnnotation.code();
        String message = httpExceptionAnnotation.message();
        String local = httpExceptionAnnotation.local().toString();

        // добавляем аннотацию @ExceptionHandler и возвращаемый тип
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
                .addAnnotation(addExceptionHandlerAnnotation(exception))
                .returns(ResponseEntity.class);

        // если глобальная ошибка
        if (httpExceptionAnnotation.globalError().turnOn()) {
            String serviceMessage = httpExceptionAnnotation.globalError().message() + " Сообщение об ошибке: ";
            int importance = httpExceptionAnnotation.globalError().importance();
            // добавляем вызов обработки глобальной ошибки и добавления параметра ошибки e
            builder
                    .addParameter(addParameterFromElement(exception))
                    .addStatement("$N.addNewGlobalError($L, $S + $L, $L, $L)",
                            addFieldFromClassName(codeGlobalErrorClassName),
                            code,
                            serviceMessage, toLowerCaseFirstLetter(exception.getSimpleName().toString()) + ".getMMessage()",
                            toLowerCaseFirstLetter(exception.getSimpleName().toString()) + ".getStackTraceElements()",
                            importance);
        }

        // добавляет return
        builder.addStatement("return new $T().generateError($L, $L, $S, $L)",
                getResponseClass(exception),
                httpStatus,
                code,
                message,
                local);
        return builder.build();
    }

    /**
     * Получаем ResponseClass, указанный как параметр аннотации @HttpException
     *
     * @param element элемент, аннотированный @HttpException
     * @return TypeElement класса-ответа
     */
    @Nullable
    private TypeElement getResponseClass (Element element) {
        TypeElement typeElement = null;
        try {
            Class<?> mClass = element.getAnnotation(HttpException.class).responseClass();
        } catch (MirroredTypeException mte){
            typeElement = asTypeElement(typeUtils, mte.getTypeMirror());
        }
        return typeElement;
    }

    /**
     * Добавляем параметры по классу, представленному типом Element
     *
     * @param element класс параметра
     * @return ParameterSpec параметра
     */
    private ParameterSpec addParameterFromElement(Element element) {
        TypeName elementTypeName = TypeName.get(element.asType());
        return ParameterSpec
                .builder(elementTypeName, toLowerCaseFirstLetter(element.getSimpleName().toString()))
                .build();
    }

    /**
     * Добавляем аннотацию @ExceptionHandler с классов, описанным Element в роли значения
     *
     * @param element класс ошибки
     * @return AnnotationSpec аннотации
     */
    private AnnotationSpec addExceptionHandlerAnnotation(Element element) {
        return AnnotationSpec.builder(ExceptionHandler.class)
                .addMember("value", "$T.class", element)
                .build();
    }
}
