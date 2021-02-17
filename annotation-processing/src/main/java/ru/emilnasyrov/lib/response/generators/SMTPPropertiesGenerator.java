package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.buildEmptyConstructor;

public class SMTPPropertiesGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    public SMTPPropertiesGenerator (Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
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
                        getSmtpPropertiesPackage(rootElementPackage),
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
        return TypeSpec.classBuilder(getSmtpPropertiesName())
                .addModifiers(Modifier.PUBLIC)
                .addFields(buildPropertyFields())
                .addAnnotation(buildConfigurationAnnotation("ResponseLibSMTPProperties"))
                .addAnnotation(buildConfigurationPropertiesAnnotation("response.lib.smtp"))
                .addMethod(buildEmptyConstructor())
                .addMethod(buildAllArgsConstructor(buildPropertyFields()))
                .addMethods(buildGettersAndSetters(buildPropertyFields()))
                .build();
    }

    private AnnotationSpec buildConfigurationPropertiesAnnotation (String name) {
        return AnnotationSpec.builder(ConfigurationProperties.class)
                .addMember("value", "\"" + name + "\"")
                .build();
    }

    private AnnotationSpec buildConfigurationAnnotation(String name){
        return AnnotationSpec.builder(Configuration.class)
                .addMember("value", "\"" + name + "\"")
                .build();
    }

    private List<FieldSpec> buildPropertyFields () {
        List<FieldSpec> columnFields = new ArrayList<>();
        columnFields.add(hostField());
        columnFields.add(portField());
        columnFields.add(userField());
        columnFields.add(passwordField());
        columnFields.add(titleField());
        columnFields.add(sslField());
        columnFields.add(debugField());
        return columnFields;
    }

    private MethodSpec buildAllArgsConstructor (List<FieldSpec> fieldSpecs) {
        MethodSpec.Builder builder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (FieldSpec fieldSpec : fieldSpecs) {
            builder
                    .addParameter(convertFieldToParameter(fieldSpec))
                    .addStatement("this.$N = $N", fieldSpec, fieldSpec);
        }
        return builder.build();
    }

    private FieldSpec hostField () {
        return FieldSpec.builder(String.class, "host")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec portField () {
        return FieldSpec.builder(int.class, "port")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec userField () {
        return FieldSpec.builder(String.class, "user")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec passwordField () {
        return FieldSpec.builder(String.class, "password")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec titleField () {
        return FieldSpec.builder(String.class, "title")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec sslField () {
        return FieldSpec.builder(boolean.class, "ssl")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec debugField () {
        return FieldSpec.builder(boolean.class, "debug")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }
}
