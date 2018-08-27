package com.yada.ssp.pushServer;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FcmTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource("serviceAccountKey.json").getInputStream()))
                .build();
        FirebaseApp.initializeApp(options);

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setTtl(100)
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("content-available", "1");
        String token = "deia5e_zLPs:APA91bF9DMTsXAfVlxHl_Vk_cTN2mQp3F7-kTtM8VxCuH7ZjPe0KGDIGvvwuXUYVr0xvyfJWhshJ-zPE1ZwyEq0ZgWzLqrlZUt94-B1dUASqzLVFTfDQl0l9HZLql-42SXhplw4hEBtWCRNbsHFDFHOANWvi63CXoA";
        Message message = Message.builder().putAllData(data).setToken(token)
                .setAndroidConfig(androidConfig)
                .setNotification(new Notification("Title", "This is body"))
                .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        System.out.println("Sent message: " + response);
    }
}
