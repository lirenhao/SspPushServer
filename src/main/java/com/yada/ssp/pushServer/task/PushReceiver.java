package com.yada.ssp.pushServer.task;

import com.yada.ssp.pushServer.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class PushReceiver {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private PushService pushService;

    @Autowired
    public PushReceiver(PushService pushService) {
        this.pushService = pushService;
    }

    @JmsListener(destination = "${mq.tranQueue}", containerFactory = "mqFactory")
    public void tranMessage(byte[] msg) {
        logger.info("MQ获取的信息[%s]", new String(msg));
        pushService.push(pushService.strToMap(new String(msg)));
    }

    @JmsListener(destination = "${mq.throwQueue}", containerFactory = "mqFactory")
    public void throwMessage(byte[] msg) {
        logger.info("MQ获取的信息[%s]", new String(msg));
    }
}
