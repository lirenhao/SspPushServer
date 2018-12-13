package com.yada.ssp.pushServer;

import com.yada.ssp.pushServer.service.PushService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PushServerApplication.class)
public class PushServerApplicationTests {

    @Autowired
    private PushService pushService;

    @Test
    public void sendFcm() {
        String token = "ftxJn3_D-4U:APA91bG7K9CtuAKT7oj5PTgCC51C0_QFTPou4NEldNpyuqpMrAHzwAtPBHV-omvMYinfKOCEgKzYs8a7PB_4rEgv740A5yBsEOtnmXOSNetL8SkkFzyjuWKhmqbVT4AptQ5Vvpx3JIJX";
        Map<String, String> data = new HashMap<>();
        data.put("deviceNo", token);
        data.put("merNo", "104767011000006");
        data.put("tranDate", "20180907162134");
        data.put("tranType", "tranType");
        data.put("channel", "Union Pay");
        data.put("tranAmt", "10.00");
        data.put("tranCry", "￥");
        data.put("traceNo", "201812071648230002");
        data.put("rrn", "rrn");
        pushService.sendFcm(data);
    }

    @Test
    public void sendApns() {
        String token = "1e768ff087d606248d816332053a6303fd821a22c6cfaa02df09012049b1ca0c";
        Map<String, String> data = new HashMap<>();
        data.put("deviceNo", token);
        data.put("merNo", "123456789012345");
        data.put("tranDate", "20180914084823");
        data.put("tranType", "tranType");
        data.put("channel", "微信支付");
        data.put("tranAmt", "10.00");
        data.put("tranCry", "￥");
        data.put("traceNo", "201809140848230001");
        data.put("rrn", "rrn");
        pushService.sendApns(data);
    }
}
