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
import com.yada.ssp.pushServer.config.FcmProperties;
import com.yada.ssp.pushServer.config.MqProperties;
import com.yada.ssp.pushServer.dao.DeviceDao;
import com.yada.ssp.pushServer.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PushService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${push.title}")
    private String pushTitle;
    @Value("${push.body}")
    private String pushBody;

    private final DeviceDao deviceDao;
    private final ApnsClient apnsClient;
    private final NotifyErrService notifyErrService;
    private final MqProperties mqProperties;
    private final ApnsProperties apnsProperties;
    private final FcmProperties fcmProperties;

    @Autowired
    public PushService(DeviceDao deviceDao, ApnsClient apnsClient, NotifyErrService notifyErrService,
                       MqProperties mqProperties, ApnsProperties apnsProperties, FcmProperties fcmProperties) {
        this.deviceDao = deviceDao;
        this.apnsClient = apnsClient;
        this.notifyErrService = notifyErrService;
        this.mqProperties = mqProperties;
        this.apnsProperties = apnsProperties;
        this.fcmProperties = fcmProperties;
    }

    @Async
    public void push(String notify) {
        Map<String, String> data = tranToMap(notify);
        List<Device> devices = deviceDao.findByMerNo(data.get("merNo"));
        push(devices, data);
    }

    @Async
    public void pushErr(String notify) {
        Map<String, String> data = strToMap(notify);
        List<Device> devices = deviceDao.findByDeviceNo(data.get("deviceNo"));
        if(devices.size() > 0) {
            push(devices, data);
        } else {
            notifyErrService.delete(notify);
        }
    }

    private void push(List<Device> devices, Map<String, String> data){
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
        data.put("deviceNo", deviceToken);
        ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertTitle(pushTitle);
        payloadBuilder.setAlertBody(pushBody);
        payloadBuilder.setContentAvailable(true);
        payloadBuilder.addCustomProperty("data", data);

        String payload = payloadBuilder.buildWithDefaultMaximumLength();
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                deviceToken, apnsProperties.getTopic(), payload,
                new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1)), DeliveryPriority.IMMEDIATE,
                data.get("tranNo"), UUID.randomUUID());
        try {
            if (!apnsClient.sendNotification(pushNotification).get().isAccepted()) {
                logger.warn("APNS推送消息失败,设备码是[{}]", deviceToken);
                // 存储数据库
                notifyErrService.next(mapToStr(data));
            } else {
                notifyErrService.delete(mapToStr(data));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("APNS推送消息异常,设备码是[{}],异常信息是[{}]", deviceToken, e.getMessage());
            // 存储数据库
            notifyErrService.next(mapToStr(data));
        }
    }

    public void sendFcm(String deviceToken, Map<String, String> data) {
        data.put("deviceNo", deviceToken);
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(fcmProperties.getTtl())
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey(data.get("tranNo"))
                .build();

        Message message = Message.builder()
                .putAllData(data)
                .setToken(deviceToken)
                .setAndroidConfig(androidConfig)
                .setNotification(new Notification(pushTitle, pushBody))
                .build();

        try {
            FirebaseMessaging.getInstance().sendAsync(message).get();
            notifyErrService.delete(mapToStr(data));
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("FCM推送消息异常,设备码是[{}],异常信息是[{}]", deviceToken, e.getMessage());
            // 存储数据库
            notifyErrService.next(mapToStr(data));
        }
    }

    /**
     * 拆分数据
     *
     * @param data MQ获取的数据
     * @return 字段映射
     */
    Map<String, String> tranToMap(String data) {
        // 商户号、交易时间(YYYYMMDDmmhhss)、支付方式、交易渠道、交易金额、交易币种、交易单号、检索参考号
        String[] fields = mqProperties.getDataField();
        Map<String, String> tran = new LinkedHashMap<>();
        try {
            Pattern pattern = Pattern.compile(mqProperties.getDataRegex());
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                for (int i = 0; i < fields.length; i++) {
                    tran.put(fields[i], matcher.group(i + 1).trim());
                }
            } else {
                logger.warn("推送数据解析失败,数据是[{}]", data);
            }
        } catch (Exception e) {
            logger.warn("推送数据解析失败,数据是[{}]", data, e.getMessage());
        }
        return tran;
    }

    String mapToStr(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            sb.append(key).append("=").append(data.get(key)).append("&");
        }
        return sb.toString();
    }

    /**
     * 拆分数据
     *
     * @param data 发送失败的数据
     * @return 字段映射
     */
    Map<String, String> strToMap(String data) {
        String[] values = data.split("&");
        Map<String, String> tran = new LinkedHashMap<>();
        for (String value : values) {
            String[] param = value.split("=");
            tran.put(param[0], param[1]);
        }
        return tran;
    }
}
