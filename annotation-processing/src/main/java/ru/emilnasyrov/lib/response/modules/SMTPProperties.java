package ru.emilnasyrov.lib.response.modules;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "response.lib.smtp"
)
public class SMTPProperties {
    private String host;
    private int port;
    private String user;
    private String password;
    private String title;
    private boolean ssl = false;
    private boolean debug = false;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getTitle() {
        return title;
    }

    public boolean getSsl(){
        return ssl;
    }

    public boolean getDebug(){
        return debug;
    }
}
