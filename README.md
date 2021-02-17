# ResponseLib
ResponseLib - это библиотека, позволяющая стандартизировать вывод ошибочных ответов от back-end на spring boot.

Использование:
1) В данный момент необходимо использовать аннотацию @SpringBootApplication из org.springframework.boot как начальную отправную точку для файлов, сгенерированных библиотекой. В данный момент отсутствует поддержка использования каких-либо иных способов конфигурации spring
2) Создайте класс, расширяющий RuntimeException
3) Повесьте на класс аннотацию @HttpException и определите параметры
4) Аннотация @HttpException имеет следующие обязательные параметры:
- code - код ответа. Необходим для того, чтобы front-end мог корректно обработать вашу конкретную ошибку
- message - сообщение, которое будет отображено front-end-у. Необходимо для того, чтобы его можно было вывести по дефолту в случае, что front-end не обработал вашу ошибку по коду
5) Необязательными параметрами являются:
- locals - язык, на котором написано сообщение
- responseClass - класс, конфигурирующий ответ. Должен наследоваться от класса AbstractResponse и переропределять метод generateError
- addGlobalError - аннотация, определяющая необходимость добавить обработку глобальных ошибок
6) Аннотация @GlobalError - аннотация, использующаяся как поданнотация аннотации @HttpException, позволяет указать необходимость использовать обработчик глобальных ошибок для данной ошибки. Что это означает:
    1. Будут автоматически созданы 2 таблицы: global_errors и notification_emails. В первую из них будет сохраняться иформация каждый раз, когда данный exception, помечанный аннотацией @GlobalError, будет вызван, во вторую необходимо будет занести информацию о почтах, на которые необходимо делать рассылки каждый раз, когда какая-либо из ошибок, помечанных аннотацией @GlobalError, будет вызываться в коде
    2. Будут автоматически созданы файлы, необходимые для обработки ошибок
    3. Вызов таких ошибок будет соправождаться отправкой письма на почты, указанные в таблице notification_emails
    4. Для корректной работоспособности этого функционала необходимо создать smtp запись и записать данные о ней в проперти файл: response.lib.smtp.host, response.lib.smtp.port, response.lib.smtp.user, response.lib.smtp.password, response.lib.smtp.title, response.lib.smtp.ssl, response.lib.smtp.debug. Где title - строка, которая будет печататься в заголовке с письмами, ssl - включить/выключить передачу по ssl, debug - если true, то в консоле будет выводиться debug информация

Добавьте следующие зависимости:
- Валидация поты:
```
<dependency>
    <groupId>commons-validator</groupId>
    <artifactId>commons-validator</artifactId>
    <version>1.4.1</version>
</dependency>
```
- Отправка писем
```
<dependency>
    <groupId>javax.mail</groupId>
    <artifactId>mail</artifactId>
    <version>1.5.0-b01</version>
</dependency>
```
- SpringBoot mail
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
    <version>2.4.2</version>
</dependency>
```
- JavaPoet
```
<dependency>
    <groupId>com.squareup</groupId>
    <artifactId>javapoet</artifactId>
    <version>1.13.0</version>
</dependency>
```
Для работы в Gradle. annotationProcessor указывает какие бибилиотеки должны быть доступны на уровне компиляции
```
compileOnly 'ru.emilnasyrov.lib:response:1.0'
annotationProcessor 'ru.emilnasyrov.lib:response:1.0'
implementation group: 'commons-validator', name: 'commons-validator', version: '1.7'
implementation group: 'javax.mail', name: 'mail', version: '1.5.0-b01'
implementation group: 'org.springframework.boot', name: 'spring-boot-starter-mail', version: '2.4.2'
implementation group: 'org.springframework', name: 'spring-web', version: '5.3.4'
implementation('org.springframework.boot:spring-boot-starter-web')
annotationProcessor group: 'org.springframework', name: 'spring-web', version: '5.3.4'
annotationProcessor group: 'org.springframework.boot', name: 'spring-boot-autoconfigure', version: '2.4.2'
annotationProcessor group: 'com.squareup', name: 'javapoet', version: '1.13.0'
annotationProcessor group: 'javax.persistence', name: 'javax.persistence-api', version: '2.2'
annotationProcessor group: 'commons-validator', name: 'commons-validator', version: '1.7'
annotationProcessor 'org.springframework.boot:spring-boot-starter-data-jpa'
annotationProcessor group: 'org.springframework.boot', name: 'spring-boot-starter-mail', version: '2.4.2'
annotationProcessor('org.springframework.boot:spring-boot-starter-web')
```
