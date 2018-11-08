package com.yada.ssp.pushServer.config;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.yada.ssp.pushServer.client.ApnsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;

@Configuration
public class PushConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean(destroyMethod = "close")
    public ApnsClient apnsClient(ApnsProperties apnsProperties, ProxyProperties proxyProperties)
            throws IOException, GeneralSecurityException {
        return new ApnsClient(apnsProperties, proxyProperties);
    }

    @Bean
    public FirebaseApp fcm(FcmProperties properties, ProxyProperties proxyProperties)
            throws IOException, GeneralSecurityException {
        FirebaseOptions.Builder optionsBuilder = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(properties.getFilePath()).getInputStream()));

        if (proxyProperties.isEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort()));
            HttpTransport transport = new NetHttpTransport.Builder()
                    .doNotValidateCertificate()
                    .setProxy(proxy)
                    .build();

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ClassPathResource(properties.getFilePath()).getInputStream(), () -> transport);

            logger.info("FCM开启代理设置,代理地址是[{}]", proxy.toString());
            optionsBuilder.setCredentials(credentials).setHttpTransport(transport);
        }
        return FirebaseApp.initializeApp(optionsBuilder.build());
    }
}
