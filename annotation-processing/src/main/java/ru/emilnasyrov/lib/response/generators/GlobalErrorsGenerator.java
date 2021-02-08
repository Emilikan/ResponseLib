package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class GlobalErrorsGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    public GlobalErrorsGenerator (Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.messager = messager;
        this.rootElement = rootElement;
        this.typeUtils = typeUtils;
        this.rootElementPackage = rootElement.getEnclosingElement().toString();
    }

    public void generate () throws Throwable {
        JavaFile javaFile = JavaFile
                .builder(
                        getGlobalErrorsPackage(rootElementPackage),
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
        return TypeSpec
                .classBuilder(getGlobalErrorsName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addAnnotation(buildTableAnnotation("global_errors"))
                .addModifiers(Modifier.PUBLIC)
                .addFields(buildColumnFields())
                .addMethod(buildEmptyConstructor())
                .addMethod(buildAllArgsConstructor())
                .addMethods(buildGettersAndSetters(buildColumnFields()))
                .build();
    }

    private MethodSpec buildAllArgsConstructor () {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(convertFieldToParameter(codeField()))
                .addParameter(convertFieldToParameter(messageField()))
                .addParameter(convertFieldToParameter(locationField()))
                .addParameter(convertFieldToParameter(stackTraceField()))
                .addParameter(convertFieldToParameter(importanceField()))
                .addStatement("this.$N = $N", codeField(), codeField())
                .addStatement("this.$N = $N", messageField(), messageField())
                .addStatement("this.$N = $N", locationField(), locationField())
                .addStatement("this.$N = $N", importanceField(), importanceField())
                .addStatement("this.$N = new $T()", dateField(), Date.class)
                .addStatement("this.$N = $N", stackTraceField(), stackTraceField())
                .build();
    }

    private List<FieldSpec> buildColumnFields () {
        List<FieldSpec> columnFields = new ArrayList<>();
        columnFields.add(idField());
        columnFields.add(codeField());
        columnFields.add(messageField());
        columnFields.add(locationField());
        columnFields.add(stackTraceField());
        columnFields.add(importanceField());
        columnFields.add(dateField());
        return columnFields;
    }

    private FieldSpec dateField () {
        return FieldSpec.builder(Date.class, "date")
                .addAnnotation(buildColumnAnnotation("date", true, ""))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec importanceField () {
        return FieldSpec.builder(int.class, "importance")
                .addAnnotation(buildColumnAnnotation("importance", false, ""))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec stackTraceField () {
        return FieldSpec.builder(String.class, "stackTrace")
                .addAnnotation(buildColumnAnnotation("stack_trace", true, "text"))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec locationField () {
        return FieldSpec.builder(String.class, "location")
                .addAnnotation(buildColumnAnnotation("location", false, "text"))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec idField () {
        return FieldSpec.builder(Long.class, "id")
                .addAnnotation(Id.class)
                .addAnnotation(
                        AnnotationSpec.builder(GeneratedValue.class)
                                .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                                .build()
                )
                .addAnnotation(buildColumnAnnotation("id", true, ""))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec codeField () {
        return FieldSpec.builder(int.class, "code")
                .addAnnotation(buildColumnAnnotation("code", false, ""))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec messageField () {
        return FieldSpec.builder(String.class, "message")
                .addAnnotation(buildColumnAnnotation("message", false, "text"))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }


}
