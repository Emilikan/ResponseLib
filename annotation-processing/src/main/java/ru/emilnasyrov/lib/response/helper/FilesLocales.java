package ru.emilnasyrov.lib.response.helper;

public class FilesLocales {
    public static final String LIBRARY_SUFFIX = ".response.lib";

    private static final String CODE_GLOBAL_ERROR_SERVICE_SUFFIX = ".service";
    private static final String CODE_GLOBAL_ERROR_SERVICE_NAME = "CodeGlobalErrorService";

    private static final String AWESOME_EXCEPTION_HANDLER_SUFFIX = ".handler";
    private static final String AWESOME_EXCEPTION_HANDLER_NAME = "AwesomeExceptionHandler";

    private static final String GLOBAL_ERRORS_SUFFIX = ".entities";
    private static final String GLOBAL_ERRORS_NAME = "GlobalErrors";

    private static final String NOTIFICATION_EMAILS_SUFFIX = ".entities";
    private static final String NOTIFICATION_EMAILS_NAME = "NotificationEmails";

    private static final String GLOBAL_ERRORS_REPOSITORY_SUFFIX = ".repositories";
    private static final String GLOBAL_ERRORS_REPOSITORY_NAME = "GlobalErrorsRepository";

    private static final String NOTIFICATION_EMAILS_REPOSITORY_SUFFIX = ".repositories";
    private static final String NOTIFICATION_EMAILS_REPOSITORY_NAME = "NotificationEmailsRepository";

    private static final String SMTP_PROPERTIES_SUFFIX = ".properties";
    private static final String SMTP_PROPERTIES_NAME = "SMTPProperties";

    public static String getCodeGlobalErrorServiceFullName(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + CODE_GLOBAL_ERROR_SERVICE_SUFFIX + "." + CODE_GLOBAL_ERROR_SERVICE_NAME;
    }

    public static String getCodeGlobalErrorServicePackage(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + CODE_GLOBAL_ERROR_SERVICE_SUFFIX;
    }

    public static String getCodeGlobalErrorServiceName(){
        return CODE_GLOBAL_ERROR_SERVICE_NAME;
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

    public static String getGlobalErrorsName(){
        return GLOBAL_ERRORS_NAME;
    }

    public static String getGlobalErrorsFullName(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + GLOBAL_ERRORS_SUFFIX + "." + GLOBAL_ERRORS_NAME;
    }

    public static String getGlobalErrorsPackage(String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + GLOBAL_ERRORS_SUFFIX;
    }

    public static String getNotificationEmailsName (){
        return NOTIFICATION_EMAILS_NAME;
    }

    public static String getNotificationEmailsFullName (String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + NOTIFICATION_EMAILS_SUFFIX + "." + NOTIFICATION_EMAILS_NAME;
    }

    public static String getNotificationEmailsPackage (String rootPackage){
        return rootPackage + LIBRARY_SUFFIX + NOTIFICATION_EMAILS_SUFFIX;
    }

    public static String getNotificationEmailsRepositoryName () {
        return NOTIFICATION_EMAILS_REPOSITORY_NAME;
    }

    public static String getNotificationEmailsRepositoryFullName (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + NOTIFICATION_EMAILS_REPOSITORY_SUFFIX + "." + NOTIFICATION_EMAILS_REPOSITORY_NAME;
    }

    public static String getNotificationEmailsRepositoryPackage (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + NOTIFICATION_EMAILS_REPOSITORY_SUFFIX;
    }

    public static String getGlobalErrorsRepositoryName () {
        return GLOBAL_ERRORS_REPOSITORY_NAME;
    }

    public static String getGlobalErrorsRepositoryFullName (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + GLOBAL_ERRORS_REPOSITORY_SUFFIX + "." + GLOBAL_ERRORS_REPOSITORY_NAME;
    }

    public static String getGlobalErrorsRepositoryPackage (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + GLOBAL_ERRORS_REPOSITORY_SUFFIX;
    }

    public static String getSmtpPropertiesName () {
        return SMTP_PROPERTIES_NAME;
    }

    public static String getSmtpPropertiesFullName (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + SMTP_PROPERTIES_SUFFIX + "." + SMTP_PROPERTIES_NAME;
    }

    public static String getSmtpPropertiesPackage (String rootPackage) {
        return rootPackage + LIBRARY_SUFFIX + SMTP_PROPERTIES_SUFFIX;
    }


}
