package com.yada.ssp.pushServer.model;

import javax.persistence.*;

@Entity
@Table(name = "T_L_APP_NOTIFY_ERR")
public class NotifyErr {

    @Id
    private String id;
    // 发送的数据
    @Column(length = 1024)
    private String notifydata;
    // 上次发送的时间
    @Column
    private String datetime;
    // 重复发送的次数
    @Column
    private int retryNo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotifydata() {
        return notifydata;
    }

    public void setNotifydata(String notifydata) {
        this.notifydata = notifydata;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getRetryNo() {
        return retryNo;
    }

    public void setRetryNo(int retryNo) {
        this.retryNo = retryNo;
    }
}
