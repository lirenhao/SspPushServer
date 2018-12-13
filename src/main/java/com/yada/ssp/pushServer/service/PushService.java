package com.yada.ssp.pushServer.service;

import com.google.firebase.messaging.*;
import com.yada.ssp.pushServer.client.ApnsClient;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        List<Device> devices = deviceDao.findDevices(data.get("merNo"), data.get("termNo"));
        push(devices, data);
    }

    @Async
    public void pushErr(String id, String notify) {
        Map<String, String> data = strToMap(notify);
        List<Device> devices = deviceDao.findByTermNoAndDeviceNo(data.get("termNo"), data.get("deviceNo"));
        if (devices.size() > 0) {
            data.put("id", id);
            push(devices, data);
        } else {
            logger.warn("重发时没有查询到设备信息,商户号是[{}],终端号是[{}],设备码是[{}]",
                    data.get("merNo"), data.get("termNo"), data.get("deviceNo"));
            notifyErrService.delete(data.get("id"));
        }
    }

    private void push(List<Device> devices, Map<String, String> data) {
        for (Device device : devices) {
            data.put("deviceNo", device.getDeviceNo());
            switch (device.getPushType()) {
                case "FCM":
                    sendFcm(data);
                    break;
                case "APNS":
                    sendApns(data);
                    break;
            }
        }
    }

    public void sendApns(Map<String, String> data) {
        if (apnsClient.send(pushTitle, pushBody, data)) {
            notifyErrService.delete(data.get("id"));
        } else {
            notifyErrService.next(data.get("id"), mapToStr(data));
        }
    }

    public void sendFcm(Map<String, String> data) {
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(fcmProperties.getTtl())
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey(data.get("traceNo"))
                .build();

        String deviceNo = data.get("deviceNo");
        data.remove("deviceNo");

        Message message = Message.builder()
                .putAllData(data)
                .putData("title", pushTitle)
                .putData("message", pushBody)
                .putData("notId", data.get("traceNo"))
                .putData("content-available", "1")
                .setToken(deviceNo)
                .setAndroidConfig(androidConfig)
                .build();

        try {
            String resp = FirebaseMessaging.getInstance().send(message);
            logger.info("FCM推送消息成功,设备码是[{}],返回信息是[{}]", deviceNo, resp);
            notifyErrService.delete(data.get("id"));
        } catch (FirebaseMessagingException e) {
            logger.warn("FCM推送消息失败,设备码是[{}],失败信息是[{}]", deviceNo, e.getMessage());
            // 存储数据库
            notifyErrService.next(data.get("id"), mapToStr(data));
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
            logger.warn("推送数据解析失败,数据是[{}],异常信息是[{}]", data, e.getMessage());
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
