package com.yada.ssp.pushServer.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
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

    @Bean
    public ApnsClient apnsClient(ApnsProperties properties, ProxyProperties proxyProperties) throws IOException {
        if (proxyProperties.isEnabled()) {
            InetSocketAddress address = new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort());
            HttpProxyHandlerFactory factory = new HttpProxyHandlerFactory(address);

            logger.info("APNS开启代理设置,代理地址是[{}]", address.toString());
            return new ApnsClientBuilder()
                    .setApnsServer("dev".equals(properties.getEnv()) ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                    .setClientCredentials(new ClassPathResource(properties.getCertPath()).getInputStream(), properties.getCertPwd())
                    .setTrustedServerCertificateChain(new ClassPathResource("GeoTrust_Global_CA.pem").getInputStream())
                    .setProxyHandlerFactory(factory)
                    .build();
        } else {
            return new ApnsClientBuilder()
                    .setApnsServer("dev".equals(properties.getEnv()) ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                    .setClientCredentials(new ClassPathResource(properties.getCertPath()).getInputStream(), properties.getCertPwd())
                    .setTrustedServerCertificateChain(new ClassPathResource("GeoTrust_Global_CA.pem").getInputStream())
                    .build();
        }
    }

    @Bean
    public FirebaseApp fcm(FcmProperties properties, ProxyProperties proxyProperties) throws IOException, GeneralSecurityException {
        if (proxyProperties.isEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort()));
            HttpTransport transport = new NetHttpTransport.Builder().doNotValidateCertificate().setProxy(proxy).build();

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ClassPathResource(properties.getFilePath()).getInputStream(), () -> transport);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(credentials)
                    .setHttpTransport(transport)
                    .build();

            logger.info("FCM开启代理设置,代理地址是[{}]", proxy.toString());
            return FirebaseApp.initializeApp(options);
        } else {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(properties.getFilePath()).getInputStream()))
                    .setHttpTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .build();
            return FirebaseApp.initializeApp(options);
        }
    }
}
