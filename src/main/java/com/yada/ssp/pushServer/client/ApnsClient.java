package com.yada.ssp.pushServer.client;

import com.clevertap.apns.Notification;
import com.clevertap.apns.NotificationResponse;
import com.turo.pushy.apns.DeliveryPriority;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.yada.ssp.pushServer.config.ApnsProperties;
import com.yada.ssp.pushServer.config.ProxyProperties;
import io.netty.channel.nio.NioEventLoopGroup;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ApnsClient {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ApnsProperties apnsProperties;
    private com.clevertap.apns.ApnsClient okClient;
    private com.turo.pushy.apns.ApnsClient nettyClient;

    public ApnsClient(ApnsProperties apnsProperties, ProxyProperties proxyProperties)
            throws IOException, GeneralSecurityException {
        this.apnsProperties = apnsProperties;
        if ("token".equals(apnsProperties.getType())) {
            this.okClient = createOkClient(apnsProperties, proxyProperties);
            this.nettyClient = null;
        } else {
            this.okClient = null;
            this.nettyClient = createNettyClient(apnsProperties, proxyProperties);
        }

    }

    private com.clevertap.apns.ApnsClient createOkClient(ApnsProperties apnsProperties, ProxyProperties proxyProperties)
            throws GeneralSecurityException, IOException {
        com.clevertap.apns.clients.ApnsClientBuilder clientBuilder = new com.clevertap.apns.clients.ApnsClientBuilder()
                .withKeyID(apnsProperties.getKeyId())
                .withTeamID(apnsProperties.getTeamId())
                .withApnsAuthKey(apnsProperties.getAuthKey())
                .withPassword(apnsProperties.getCertPwd())
                .inSynchronousMode();

        logger.info("APNS使用OkHttpClient,连接的环境是[{}]", apnsProperties.getEnv());
        if ("dev".equals(apnsProperties.getEnv())) {
            clientBuilder.withDevelopmentGateway();
        } else {
            clientBuilder.withProductionGateway();
        }

        // TLS握手时不校验证书域名,只能使用APNs的token认证模式
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, trustAllCerts, null);
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .hostnameVerifier((arg0, arg1) -> true)
                .sslSocketFactory(sslContext.getSocketFactory());

        if (proxyProperties.isEnabled()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort()));
            httpBuilder.proxy(proxy);
            logger.info("APNS开启代理设置,代理地址是[{}]", proxy.toString());
        }
        clientBuilder.withOkHttpClientBuilder(httpBuilder);

        return clientBuilder.build();
    }

    private com.turo.pushy.apns.ApnsClient createNettyClient(ApnsProperties apnsProperties, ProxyProperties proxyProperties)
            throws IOException {
        String apnsServer = "dev".equals(apnsProperties.getEnv()) ?
                com.turo.pushy.apns.ApnsClientBuilder.DEVELOPMENT_APNS_HOST :
                com.turo.pushy.apns.ApnsClientBuilder.PRODUCTION_APNS_HOST;

        logger.info("APNS使用NettyClient,连接的环境是[{}]", apnsServer);

        if (proxyProperties.isEnabled()) {
            InetSocketAddress address = new InetSocketAddress(proxyProperties.getHost(), proxyProperties.getPort());
            HttpProxyHandlerFactory factory = new HttpProxyHandlerFactory(address);

            logger.info("APNS开启代理设置,代理地址是[{}]", address.toString());
            return new com.turo.pushy.apns.ApnsClientBuilder()
                    .setApnsServer(apnsServer)
                    .setClientCredentials(new ClassPathResource(apnsProperties.getCertPath()).getInputStream(), apnsProperties.getCertPwd())
                    .setTrustedServerCertificateChain(new ClassPathResource("root.pem").getInputStream())
                    .setProxyHandlerFactory(factory)
                    .setConcurrentConnections(4).setEventLoopGroup(new NioEventLoopGroup(4))
                    .build();
        } else {
            return new com.turo.pushy.apns.ApnsClientBuilder()
                    .setApnsServer(apnsServer)
                    .setClientCredentials(new ClassPathResource(apnsProperties.getCertPath()).getInputStream(), apnsProperties.getCertPwd())
                    .setTrustedServerCertificateChain(new ClassPathResource("root.pem").getInputStream())
                    .setConcurrentConnections(4).setEventLoopGroup(new NioEventLoopGroup(4))
                    .build();
        }
    }

    public boolean send(String pushTitle, String pushBody, Map<String, String> data) {
        boolean result = false;
        if (nettyClient != null) {
            ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertTitle(pushTitle);
            payloadBuilder.setAlertBody(pushBody);
            payloadBuilder.setContentAvailable(true);
            payloadBuilder.addCustomProperty("data", data);

            String payload = payloadBuilder.buildWithDefaultMaximumLength();
            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                    data.get("deviceNo"), apnsProperties.getTopic(), payload,
                    new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(apnsProperties.getInvalidationTime())),
                    DeliveryPriority.IMMEDIATE, data.get("traceNo"), UUID.randomUUID());

            try {
                PushNotificationResponse resp = nettyClient.sendNotification(pushNotification).get();
                if (resp.isAccepted()) {
                    logger.info("APNS推送消息成功,设备码是[{}],返回信息是[{}]", data.get("deviceNo"), resp.toString());
                    result = true;
                } else {
                    logger.warn("APNS推送消息被拒,设备码是[{}],拒绝原因是[{}]", data.get("deviceNo"), resp.getRejectionReason());
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("APNS推送消息失败,设备码是[{}],失败信息是[{}]", data.get("deviceNo"), e.getMessage());
            }
        } else {
            Notification notification = new Notification.Builder(data.get("deviceNo"))
                    .topic(apnsProperties.getTopic())
                    .alertTitle(pushTitle)
                    .alertBody(pushBody)
                    .contentAvailable(true)
                    .customField("data", data)
                    .priority(Notification.Priority.IMMEDIATE)
                    .collapseId(data.get("traceNo"))
                    .uuid(UUID.randomUUID())
                    .build();

            NotificationResponse resp = okClient.push(notification);
            if (resp.getHttpStatusCode() == 200) {
                logger.info("APNS推送消息成功,设备码是[{}],返回信息是[{}]", data.get("deviceNo"), resp);
                result = true;
            } else {
                logger.warn("APNS推送消息失败,设备码是[{}],失败信息是[{}]", data.get("deviceNo"), resp);
            }
        }
        return result;
    }

    public void close() throws InterruptedException {
        if (nettyClient != null) {
            nettyClient.close().await();
        } else {
            okClient.getHttpClient().connectionPool().evictAll();
        }
    }
}
