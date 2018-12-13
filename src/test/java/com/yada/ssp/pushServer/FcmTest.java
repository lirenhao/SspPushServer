package com.yada.ssp.pushServer;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FcmTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, GeneralSecurityException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.2.53.182", 8081));

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("serviceAccountKey.json").getInputStream()))
                // .setHttpTransport(new NetHttpTransport.Builder().doNotValidateCertificate().setProxy(proxy).build())
                .build();
        FirebaseApp.initializeApp(options);

        Map<String, String> data = new HashMap<>();
        data.put("merNo", "123456789012345");
        data.put("tranDate", "20181207105959");
        data.put("tranType", "1");
        data.put("channel", "Union Pay");
        data.put("tranAmt", "100.00");
        data.put("tranCry", "SGD");
        data.put("traceNo", "20181204105959001");
        data.put("rrn", "66666666");
        String deviceNo = "ftxJn3_D-4U:APA91bG7K9CtuAKT7oj5PTgCC51C0_QFTPou4NEldNpyuqpMrAHzwAtPBHV-omvMYinfKOCEgKzYs8a7PB_4rEgv740A5yBsEOtnmXOSNetL8SkkFzyjuWKhmqbVT4AptQ5Vvpx3JIJX";

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(0)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setCollapseKey(data.get("traceNo"))
                .build();

        Message message = Message.builder()
                .putAllData(data)
                .putData("title", "pushTitle")
                .putData("message", "pushBody")
                .putData("notId", data.get("traceNo"))
                .putData("content-available", "1")
                .setToken(deviceNo)
                .setAndroidConfig(androidConfig)
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        System.out.println("Sent message: " + response);
    }
}
