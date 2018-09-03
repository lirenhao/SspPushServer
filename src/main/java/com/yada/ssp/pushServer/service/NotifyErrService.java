package com.yada.ssp.pushServer.service;

import com.yada.ssp.pushServer.dao.NotifyErrDao;
import com.yada.ssp.pushServer.model.NotifyErr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class NotifyErrService {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private final NotifyErrDao notifyErrDao;

    @Autowired
    public NotifyErrService(NotifyErrDao notifyErrDao) {
        this.notifyErrDao = notifyErrDao;
    }

    public void next(String data) {
        NotifyErr err = notifyErrDao.findById(data).orElse(new NotifyErr());
        err.setNotifydata(data);
        err.setDatetime(sdf.format(new Date()));
        err.setRetryNo(err.getRetryNo() + 1);
        notifyErrDao.saveAndFlush(err);
    }

    public List<NotifyErr> getNotifyErr(int num, int expire) {
        return notifyErrDao.findByRetryNoLessThanEqual(num);
    }

    public void delete(String notifydata) {
        notifyErrDao.deleteByNotifydata(notifydata);
    }
}
