package com.yada.ssp.pushServer.service;

import com.turo.pushy.apns.ApnsClient;
import com.yada.ssp.pushServer.PushServerApplication;
import com.yada.ssp.pushServer.config.ApnsProperties;
import com.yada.ssp.pushServer.config.FcmProperties;
import com.yada.ssp.pushServer.config.MqProperties;
import com.yada.ssp.pushServer.dao.DeviceDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PushServerApplication.class)
public class PushServiceTest {

    @MockBean
    private DeviceDao deviceDao;
    @MockBean
    private ApnsClient apnsClient;
    @MockBean
    private NotifyErrService notifyErrService;
    @Autowired
    private MqProperties mqProperties;
    @Autowired
    private ApnsProperties apnsProperties;
    @Autowired
    private FcmProperties fcmProperties;

    private PushService pushService;

    @Before
    public void before() {
        pushService = new PushService(deviceDao, apnsClient, notifyErrService,
                mqProperties, apnsProperties, fcmProperties);
    }

    @Test
    public void tranToMap() {
        String data = "10412341234123420180827185959                type         1          100  2                                                      1234567890    66666666";
        Map<String, String> tran = pushService.tranToMap(data);

        Assert.assertEquals("104123412341234", tran.get("merNo"));
        Assert.assertEquals("20180827185959", tran.get("tranDate"));
        Assert.assertEquals("type", tran.get("tranType"));
        Assert.assertEquals("1", tran.get("channel"));
        Assert.assertEquals("100", tran.get("tranAmt"));
        Assert.assertEquals("2", tran.get("tranCry"));
        Assert.assertEquals("1234567890", tran.get("tranNo"));
        Assert.assertEquals("66666666", tran.get("rrn"));
    }

    @Test
    public void mapToStr() {
        Map<String, String> tran = new HashMap<>();
        tran.put("merNo", "merNo");
        tran.put("tranDate", "tranDate");
        tran.put("tranType", "tranType");
        tran.put("channel", "channel");
        tran.put("tranAmt", "tranAmt");
        tran.put("tranCry", "tranCry");
        tran.put("tranNo", "tranNo");
        tran.put("rrn", "rrn");

        String data = pushService.mapToStr(tran);
        Map<String, String> param = pushService.strToMap(data);

        Assert.assertEquals(tran.get("merNo"), param.get("merNo"));
        Assert.assertEquals(tran.get("tranDate"), param.get("tranDate"));
        Assert.assertEquals(tran.get("tranType"), param.get("tranType"));
        Assert.assertEquals(tran.get("channel"), param.get("channel"));
        Assert.assertEquals(tran.get("tranAmt"), param.get("tranAmt"));
        Assert.assertEquals(tran.get("tranCry"), param.get("tranCry"));
        Assert.assertEquals(tran.get("tranNo"), param.get("tranNo"));
        Assert.assertEquals(tran.get("rrn"), param.get("rrn"));
    }

    @Test
    public void strToMap() {
        String data = "10412341234123420180827185959                type         1          100  2                                                      1234567890    66666666";
        String regex = "(.{15})(.{14})(.{20})(.{10})(.{13})(.{3})(.{64})(.{12})";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        System.out.println(matcher.find());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(matcher.group(3));
        System.out.println(matcher.group(4));
        System.out.println(matcher.group(5));
        System.out.println(matcher.group(6));
        System.out.println(matcher.group(7));
        System.out.println(matcher.group(8));
    }
}