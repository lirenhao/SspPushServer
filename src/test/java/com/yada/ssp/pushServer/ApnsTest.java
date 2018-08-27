package com.yada.ssp.pushServer;

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.TokenUtil;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ApnsTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ApnsClient apnsClient = new ApnsClientBuilder()
                .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setClientCredentials(new ClassPathResource("apns-dev-cert.p12").getInputStream(), "yada")
                .build();

        ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setAlertTitle("交易提醒");
        payloadBuilder.setAlertBody("您有一笔新交易");
        payloadBuilder.addCustomProperty("tranDate", "20180823");
        payloadBuilder.setBadgeNumber(1);// 图标展示的数字
        payloadBuilder.setContentAvailable(true);// 内容可用

        // wc
        String token = "a21054343ebe4bbdd4724f8eb11f29053cd6d3b64f013190477d6092c0e2215b";
        // lrh
//        String token = "bee078d4603e0fb31568d0c770bc133581cca20baad9b4a20e69c86be2ba86d3";

        String deviceToken = TokenUtil.sanitizeTokenString(token);
        String payload = payloadBuilder.buildWithDefaultMaximumLength();
        SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                deviceToken, "com.yada.sg.ssp", payload,
                new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1)), DeliveryPriority.IMMEDIATE,
                "trans3", UUID.randomUUID());
        // collapseId通知合并


        PushNotificationResponse<SimpleApnsPushNotification> response = apnsClient.sendNotification(pushNotification).get();
        System.out.println("Sent message: " + response.toString());

        apnsClient.close();
    }
}
