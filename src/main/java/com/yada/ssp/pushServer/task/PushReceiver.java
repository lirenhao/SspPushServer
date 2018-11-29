package com.yada.ssp.pushServer.task;

import com.yada.ssp.pushServer.model.NotifyErr;
import com.yada.ssp.pushServer.service.NotifyErrService;
import com.yada.ssp.pushServer.service.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PushReceiver {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private PushService pushService;
    private NotifyErrService notifyErrService;

    @Autowired
    public PushReceiver(PushService pushService, NotifyErrService notifyErrService) {
        this.pushService = pushService;
        this.notifyErrService = notifyErrService;
    }

    @Value("${notify.err.num}")
    private int errNum;
    @Value("${notify.err.expire}")
    private int errExpire;

    @JmsListener(destination = "${mq.tranQueue}", containerFactory = "mqFactory")
    public void tranMessage(String msg) {
        logger.info("MQ获取的信息[{}]", msg);
        pushService.push(msg);
    }

    @Scheduled(fixedRateString = "${notify.err.rate}")
    public void throwMessage() {
        List<NotifyErr> errList = notifyErrService.getNotifyErr(errNum, errExpire);
        for(NotifyErr err: errList) {
            logger.info("数据库获取的发送错误信息 >>>[{}]", err.getNotifydata());
            if(!notifyErrService.isSending(err.getId())){
                notifyErrService.setSending(err.getId());
                pushService.pushErr(err.getId(), err.getNotifydata());
            } else {
                logger.info("发送错误信息已经在发送 >>>[{}]", err.getNotifydata());
            }
        }
    }
}
