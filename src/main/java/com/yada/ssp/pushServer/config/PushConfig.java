package com.yada.ssp.pushServer.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;

@Configuration
public class PushConfig {

    @Bean
    public ApnsClient apnsClient(ApnsProperties properties, ProxyProperties proxyProperties) throws IOException {
        if (proxyProperties.isEnabled()) {
            InetSocketAddress address = new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort());
            HttpProxyHandlerFactory factory = new HttpProxyHandlerFactory(address);
            return new ApnsClientBuilder()
                    .setApnsServer("dev".equals(properties.getEnv()) ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                    .setClientCredentials(new ClassPathResource(properties.getCertPath()).getInputStream(), properties.getCertPwd())
                    .setProxyHandlerFactory(factory)
                    .build();
        } else {
            return new ApnsClientBuilder()
                    .setApnsServer("dev".equals(properties.getEnv()) ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                    .setClientCredentials(new ClassPathResource(properties.getCertPath()).getInputStream(), properties.getCertPwd())
                    .build();
        }
    }

    @Bean
    public FirebaseApp fcm(FcmProperties properties, ProxyProperties proxyProperties) throws IOException, GeneralSecurityException {
        if (proxyProperties.isEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort()));
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(properties.getFilePath()).getInputStream()))
                    .setHttpTransport(new NetHttpTransport.Builder().doNotValidateCertificate().setProxy(proxy).build())
                    .build();

            return FirebaseApp.initializeApp(options);
        } else {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(properties.getFilePath()).getInputStream()))
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}
