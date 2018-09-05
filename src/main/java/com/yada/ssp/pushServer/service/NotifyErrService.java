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

    /**
     * 存储错误处理的下一次处理数据
     *
     * @param data 存储的数据
     */
    public void next(String data) {
        NotifyErr err = notifyErrDao.findById(data).orElse(new NotifyErr());
        err.setNotifydata(data);
        err.setDatetime(sdf.format(new Date()));
        err.setRetryNo(err.getRetryNo() + 1);
        notifyErrDao.saveAndFlush(err);
    }

    /**
     * 获取没有超过次数没有过期的上次发送错误的数据
     *
     * @param num    筛选条件 要小于或等于这个次数
     * @param expire 筛选条件 加上超时秒数(s)要大于或等于当前时间
     * @return 符合条件的发送错误数据列表
     */
    public List<NotifyErr> getNotifyErr(int num, int expire) {
        // 大于超时时间前的时间
        String expireDate = sdf.format(new Date(System.currentTimeMillis() - expire * 1000));
        return notifyErrDao.findByRetryNoLessThanEqualAndDatetimeGreaterThanEqual(num, expireDate);
    }

    /**
     * 发送成功后删除发送错误数据
     *
     * @param notifydata 错误数据
     */
    public void delete(String notifydata) {
        notifyErrDao.deleteByNotifydata(notifydata);
    }
}
