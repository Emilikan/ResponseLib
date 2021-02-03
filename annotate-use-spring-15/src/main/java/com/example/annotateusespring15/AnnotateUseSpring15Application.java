package com.example.annotateusespring15;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.emilnasyrov.lib.unitpay.annotates.GlobalErrorSettings;

@SpringBootApplication
@GlobalErrorSettings(host = "smtp.yandex.ru", port = 465, user = "noreply@gamevalues.ru", password = "123noreply123")
public class AnnotateUseSpring15Application {

    public static void main(String[] args) {
        SpringApplication.run(AnnotateUseSpring15Application.class, args);
    }

}
