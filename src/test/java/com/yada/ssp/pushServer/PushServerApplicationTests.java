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
        String token = "deia5e_zLPs:APA91bF9DMTsXAfVlxHl_Vk_cTN2mQp3F7-kTtM8VxCuH7ZjPe0KGDIGvvwuXUYVr0xvyfJWhshJ-zPE1ZwyEq0ZgWzLqrlZUt94-B1dUASqzLVFTfDQl0l9HZLql-42SXhplw4hEBtWCRNbsHFDFHOANWvi63CXoA";
        Map<String, String> data = new HashMap<>();
        data.put("deviceNo", token);
        data.put("merNo", "104");
        data.put("tranDate", "20180907162134");
        data.put("tranType", "tranType");
        data.put("channel", "channel");
        data.put("tranAmt", "10.00");
        data.put("tranCry", "￥");
        data.put("tranNo", "201809021648230002");
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
        data.put("tranNo", "201809140848230001");
        data.put("rrn", "rrn");
        pushService.sendApns(data);
    }
}
