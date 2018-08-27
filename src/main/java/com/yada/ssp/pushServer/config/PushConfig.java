package com.yada.ssp.pushServer.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class PushConfig {

    @Bean
    public ApnsClient apnsClient(ApnsProperties properties) throws IOException {
        return new ApnsClientBuilder()
                .setApnsServer("dev".equals(properties.getEnv()) ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .setClientCredentials(new ClassPathResource(properties.getCertPath()).getInputStream(), properties.getCertPwd())
                .build();
    }

    @Bean
    public FirebaseApp fcm(FcmProperties properties) throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(properties.getFilePath()).getInputStream()))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
