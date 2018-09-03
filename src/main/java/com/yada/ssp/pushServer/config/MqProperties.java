package com.yada.ssp.pushServer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mq")
public class MqProperties {

    private String dataRegex;
    private String[] dataField;
    private String tranQueue;

    public String getDataRegex() {
        return dataRegex;
    }

    public void setDataRegex(String dataRegex) {
        this.dataRegex = dataRegex;
    }

    public String[] getDataField() {
        return dataField;
    }

    public void setDataField(String[] dataField) {
        this.dataField = dataField;
    }

    public String getTranQueue() {
        return tranQueue;
    }

    public void setTranQueue(String tranQueue) {
        this.tranQueue = tranQueue;
    }
}
