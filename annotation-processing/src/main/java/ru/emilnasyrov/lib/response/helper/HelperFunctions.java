package ru.emilnasyrov.lib.response.helper;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;

public class HelperFunctions {
    /**
     * Отправка error-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param messager Messager
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    public static void error(Element e, Messager messager, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    /**
     * Отправка warning-сообщения пользователю
     *
     * @param e элемент, на который будем ссылаться
     * @param messager Messager
     * @param msg сообщение
     * @param args аргументы для сообщения
     */
    public static void warning(Element e, Messager messager, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(msg, args),
                e);
    }

    /**
     * Приведение первого символа строки к нижнему регистру
     *
     * @param s строка
     * @return приведенная строка
     */
    public static String toLowerCaseFirstLetter (String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Приведение первого символа строки к верхнему регистру
     *
     * @param s строка
     * @return приведенная строка
     */
    public static String toUpperCaseFirstLetter (String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Приведение TypeMirror к TypeElement
     *
     * @param typeMirror элемент, который необходимо привести
     * @return
     */
    public static TypeElement asTypeElement(Types typeUtils, TypeMirror typeMirror) {
        return (TypeElement) typeUtils.asElement(typeMirror);
    }

    public static AnnotationSpec buildTableAnnotation(String tableName) {
        return AnnotationSpec.builder(Table.class)
                .addMember("name", "\"" + tableName + "\"")
                .build();
    }

    public static AnnotationSpec buildColumnAnnotation (String name, boolean nullable, String columnDefinition) {
        return AnnotationSpec.builder(Column.class)
                .addMember("name", "\"" + name + "\"")
                .addMember("nullable", String.valueOf(nullable))
                .addMember("columnDefinition", "\"" + columnDefinition + "\"")
                .build();
    }

    public static MethodSpec buildEmptyConstructor () {
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    public static ParameterSpec convertFieldToParameter (FieldSpec fieldSpec) {
        return ParameterSpec.builder(fieldSpec.type, fieldSpec.name).build();
    }

    public static MethodSpec buildGetter (FieldSpec fieldSpec){
        return MethodSpec.methodBuilder("get" + toUpperCaseFirstLetter(fieldSpec.name))
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldSpec.type)
                .addStatement("return this.$N", fieldSpec)
                .build();
    }

    public static MethodSpec buildSetter (FieldSpec fieldSpec){
        return MethodSpec.methodBuilder("set" + toUpperCaseFirstLetter(fieldSpec.name))
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(fieldSpec.type, fieldSpec.name)
                .addStatement("this.$N = $N", fieldSpec, fieldSpec)
                .build();
    }

    public static List<MethodSpec> buildGettersAndSetters (List<FieldSpec> fieldSpecs) {
        List<MethodSpec> gettersAndSetters = new ArrayList<>();
        for (FieldSpec fieldSpec : fieldSpecs){
            gettersAndSetters.add(buildGetter(fieldSpec));
            gettersAndSetters.add(buildSetter(fieldSpec));
        }
        return gettersAndSetters;
    }
}
