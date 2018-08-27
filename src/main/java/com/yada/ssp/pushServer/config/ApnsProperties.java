package com.yada.ssp.pushServer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "apns")
public class ApnsProperties {

    private String env;
    private String topic;
    private int invalidationTime;
    private int priority;
    private String certPath;
    private String certPwd;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getInvalidationTime() {
        return invalidationTime;
    }

    public void setInvalidationTime(int invalidationTime) {
        this.invalidationTime = invalidationTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getCertPwd() {
        return certPwd;
    }

    public void setCertPwd(String certPwd) {
        this.certPwd = certPwd;
    }
}
