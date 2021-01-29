package ru.emilnasyrov.lib.unitpay.processor;

import com.google.auto.service.AutoService;
import org.springframework.http.HttpStatus;
import ru.emilnasyrov.lib.unitpay.annotates.HttpException;
import ru.emilnasyrov.lib.unitpay.modules.ExceptionDateResponse;
import ru.emilnasyrov.lib.unitpay.modules.Locals;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.sql.rowset.spi.SyncResolver;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
@SupportedAnnotationTypes("ru.emilnasyrov.lib.unitpay.annotates.HttpException")
@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private final List<Element> annotatedOnlyForUnitpayClasses = new ArrayList<>();

    private final String packageName = "ru.emilnasyrov.lib.unitpay";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        // STEP 1 выполняем проверку элементов и подсчитываем их количество
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HttpException.class)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                annotatedOnlyForUnitpayClasses.add(annotatedElement);
            } else {
                error(annotatedElement, "Only classes can be annotated with @%s",
                        HttpException.class.getSimpleName());
                return true;
            }
        }

        try {
            writeAwesomeExceptionHandler(annotatedOnlyForUnitpayClasses);
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }

        return true;
    }

    private void writeAwesomeExceptionHandler(List<Element> annotatedClasses) throws IOException {
        String mClassName = "AwesomeExceptionHandler";

        JavaFileObject builderFile = filer.createSourceFile(mClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            // пакет файла
            //TODO заменить на путь от начального класса Spring (по аннотации)
            out.print("package ");
            out.print(packageName + ".handler");
            out.println(";");
            out.println();

            // импорты
            out.print("import ");
            out.print(packageName + ".modules.ExceptionDateResponse");
            out.println(";");
            out.println("import org.springframework.web.bind.annotation.ControllerAdvice;");
            out.println("import org.springframework.http.ResponseEntity;");
            out.println("import org.springframework.http.HttpStatus;");
            out.println("import "+packageName+".modules.Locals;");
            out.println("import org.springframework.web.bind.annotation.ExceptionHandler;");
            out.println("import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;");
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
            out.println();

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
                Locals local = element.getAnnotation(HttpException.class).locals();
                HttpStatus httpStatus = element.getAnnotation(HttpException.class).status();

                out.println();
                out.print("    @ExceptionHandler(");
                out.print(exceptionClass);
                out.println(".class)");

                out.println("    private ResponseEntity<?> handle" + exceptionClass + "() {");

//                out.println("       return generateError"+exceptionResponse+"(");
//                out.println("               HttpStatus."+httpStatus.getReasonPhrase().toUpperCase().replace(" ", "_")+",");
//                out.println("               "+code+",");
//                out.println("               \""+message+"\",");
//                out.println("               Locals."+local+");");

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
}
