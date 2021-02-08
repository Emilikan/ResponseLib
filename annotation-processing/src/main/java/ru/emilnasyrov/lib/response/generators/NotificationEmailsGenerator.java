package ru.emilnasyrov.lib.response.generators;

import com.squareup.javapoet.*;
import org.apache.commons.validator.routines.EmailValidator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ru.emilnasyrov.lib.response.helper.FilesLocales.*;
import static ru.emilnasyrov.lib.response.helper.HelperFunctions.*;

public class NotificationEmailsGenerator {
    private final Elements elementUtils;
    private final Filer filer;
    private final Messager messager;
    private final Types typeUtils;
    private final Element rootElement;
    private final String rootElementPackage;

    public NotificationEmailsGenerator (Types typeUtils, Elements elementUtils, Messager messager, Filer filer, Element rootElement) {
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
                        getNotificationEmailsPackage(rootElementPackage),
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
                .classBuilder(getNotificationEmailsName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addAnnotation(buildTableAnnotation("notification_emails"))
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
                .addParameter(convertFieldToParameter(emailField()))
                .addParameter(convertFieldToParameter(nameField()))
                .beginControlFlow("if ($T.getInstance().isValid($N))", EmailValidator.class, emailField())
                .addStatement("this.$N = $N", nameField(), nameField())
                .addStatement("this.$N = $N", emailField(), emailField())
                .endControlFlow()
                .build();
    }

    private List<FieldSpec> buildColumnFields () {
        List<FieldSpec> columnFields = new ArrayList<>();
        columnFields.add(idField());
        columnFields.add(emailField());
        columnFields.add(nameField());
        return columnFields;
    }

    private FieldSpec emailField () {
        return FieldSpec.builder(String.class, "email")
                .addAnnotation(buildColumnAnnotation("email", true, ""))
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private FieldSpec nameField () {
        return FieldSpec.builder(String.class, "name")
                .addAnnotation(buildColumnAnnotation("name", true, ""))
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
}
