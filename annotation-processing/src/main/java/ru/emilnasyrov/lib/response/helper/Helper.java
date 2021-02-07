package ru.emilnasyrov.lib.response.helper;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class Helper {
    public static final String LIBRARY_SUFFIX = ".response.lib";

    private static final String CODE_GLOBAL_ERROR_SERVICE_SUFFIX = ".service";
    private static final String CODE_GLOBAL_ERROR_SERVICE_NAME = "CodeGlobalErrorService";

    private static final String AWESOME_EXCEPTION_HANDLER_SUFFIX = ".handler";
    private static final String AWESOME_EXCEPTION_HANDLER_NAME = "AwesomeExceptionHandler";

    public static String getCodeGlobalErrorServiceFullName(String rootPackage){
        return rootPackage + CODE_GLOBAL_ERROR_SERVICE_SUFFIX + "." + CODE_GLOBAL_ERROR_SERVICE_NAME;
    }

    public static String getCodeGlobalErrorServicePackage(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + CODE_GLOBAL_ERROR_SERVICE_SUFFIX;
    }

    public static String getCodeGlobalErrorServiceName(){
        return CODE_GLOBAL_ERROR_SERVICE_NAME;
    }

    public static TypeElement getCodeGlobalErrorServiceTypeElement(Elements elementUtils, String rootElementPackage){
        return elementUtils.getTypeElement(getCodeGlobalErrorServiceFullName(rootElementPackage));
    }

    public static String getAwesomeExceptionHandlerFullName(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + AWESOME_EXCEPTION_HANDLER_SUFFIX + AWESOME_EXCEPTION_HANDLER_NAME;
    }

    public static String getAwesomeExceptionHandlerPackage(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + AWESOME_EXCEPTION_HANDLER_SUFFIX;
    }

    public static String getAwesomeExceptionHandlerName(){
        return AWESOME_EXCEPTION_HANDLER_NAME;
    }

    public static TypeElement getAwesomeExceptionHandlerTypeElement(Elements elementUtils, String rootElementPackage){
        return elementUtils.getTypeElement(getAwesomeExceptionHandlerFullName(rootElementPackage));
    }
}
