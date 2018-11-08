package com.yada.ssp.pushServer;

import com.yada.ssp.pushServer.client.ApnsClient;
import com.yada.ssp.pushServer.config.ApnsProperties;
import com.yada.ssp.pushServer.config.ProxyProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class ApnsTest {

    public static void main(String[] args)
            throws IOException, GeneralSecurityException, InterruptedException {
        ApnsProperties apnsProperties = new ApnsProperties();
        apnsProperties.setEnv("dev");
        apnsProperties.setType("");
        apnsProperties.setTopic("com.yada.sg.ssp");
        apnsProperties.setInvalidationTime(1);
        apnsProperties.setPriority(10);
        apnsProperties.setCertPath("apns-dev-cert.p12");
        apnsProperties.setCertPwd("yada");

        ProxyProperties proxyProperties = new ProxyProperties();
        proxyProperties.setEnabled(false);
        proxyProperties.setHost("10.2.53.182");
        proxyProperties.setPort(8081);

        ApnsClient apnsClient = new ApnsClient(apnsProperties, proxyProperties);

        Map<String, String> data = new HashMap<>();
        data.put("deviceNo", "b4da96a33e12ede033ee6d7b8123eadc56c50bc4da5020043847a010ac358478");
        data.put("merNo", "123456789012345");
        data.put("tranDate", "20181108105959");
        data.put("tranType", "tranType");
        data.put("channel", "扫码支付");
        data.put("tranAmt", "1.00");
        data.put("tranCry", "CNY");
        data.put("tranNo", "1234567890");
        data.put("rrn", "66666666");

        apnsClient.send("Test", "This is test", data);
        apnsClient.close();
    }
}
