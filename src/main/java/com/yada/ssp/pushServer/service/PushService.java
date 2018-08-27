package com.yada.ssp.pushServer.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.yada.ssp.pushServer.config.ApnsProperties;
import com.yada.ssp.pushServer.config.MqProperties;
import com.yada.ssp.pushServer.dao.DeviceDao;
import com.yada.ssp.pushServer.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class PushService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${push.title}")
    private String pushTitle;
    @Value("${push.body}")
    private String pushBody;

    private final DeviceDao deviceDao;
    private final ApnsClient apnsClient;
    private final JmsTemplate jmsTemplate;
    private final MqProperties mqProperties;
    private final ApnsProperties apnsProperties;

    @Autowired
    public PushService(DeviceDao deviceDao, ApnsClient apnsClient, JmsTemplate jmsTemplate,
                       MqProperties mqProperties, ApnsProperties apnsProperties) {
        this.deviceDao = deviceDao;
        this.apnsClient = apnsClient;
        this.jmsTemplate = jmsTemplate;
        this.mqProperties = mqProperties;
        this.apnsProperties = apnsProperties;
    }

    @Async
    public void push(Map<String, String> data) {
        List<Device> devices = deviceDao.findByMerNo(data.get("merNo"));
        for (Device device : devices) {
            switch (device.getPushType()) {
                case "FCM":
                    sendFcm(device.getDeviceNo(), data);
                    break;
                case "APNS":
                    sendApns(device.getDeviceNo(), data);
                    break;
            }
        }
    }

    public void sendApns(String deviceToken, Map<String, String> data) {
        ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertTitle(pushTitle);
        payloadBuilder.setAlertBody(pushBody);
        payloadBuilder.setContentAvailable(true);
        payloadBuilder.addCustomProperty("data", data);

        // TODO 消息的key生成
        String payload = payloadBuilder.buildWithDefaultMaximumLength();
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                deviceToken, apnsProperties.getTopic(), payload,
                new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1)), DeliveryPriority.IMMEDIATE,
                "", UUID.randomUUID());
        try {
            if (!apnsClient.sendNotification(pushNotification).get().isAccepted()) {
                logger.warn("APNS推送消息失败,设备码是[{}]", deviceToken);
                // 添加队列
                jmsTemplate.convertAndSend(mqProperties.getThrowQueue(), mapToStr(data));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("APNS推送消息异常,设备码是[{}],异常信息是[{}]", deviceToken, e.getMessage());
            // 添加队列
            jmsTemplate.convertAndSend(mqProperties.getThrowQueue(), mapToStr(data));
        }
    }

    public void sendFcm(String deviceToken, Map<String, String> data) {
        // TODO 消息的key生成
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(0)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey("")
                .build();

        Message message = Message.builder()
                .putAllData(data)
                .setToken(deviceToken)
                .setAndroidConfig(androidConfig)
                .setNotification(new Notification(pushTitle, pushBody))
                .build();

        try {
            FirebaseMessaging.getInstance().sendAsync(message).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("APNS推送消息异常,设备码是[{}],异常信息是[{}]", deviceToken, e.getMessage());
            // 添加队列
            jmsTemplate.convertAndSend(mqProperties.getThrowQueue(), mapToStr(data));
        }
    }

    /**
     * 拆分数据strToMap
     *
     * @param data MQ获取的数据
     * @return 字段映射
     */
    public Map<String, String> strToMap(String data) {
        // 商户号、交易时间(YYYYMMDDmmhhss)、支付方式、交易金额、交易币种、交易单号、检索参考号
        String[] fields = mqProperties.getDataField();
        String[] values = data.split(mqProperties.getDataRegex());
        Map<String, String> tran = new HashMap<>();
        try {
            for (int i = 0; i < fields.length; i++) {
                tran.put(fields[i], values[i]);
            }
        } catch (Exception e) {
            logger.error("推送数据解析失败,数据是[{}]", data);
        }
        return tran;
    }

    public String mapToStr(Map<String, String> data) {

        return "";
    }
}
