package com.yada.ssp.pushServer.model;

import javax.persistence.*;

@Entity
@Table(name = "T_B_APP_DEVICE")
@IdClass(DevicePK.class)
public class Device {

    //商户号
    @Id
    private String merNo;
    //登录名
    @Id
    private String loginName;
    //设备号
    @Column
    private String deviceNo;
    //推送方式 FCM、APNS
    @Column
    private String pushType;
    //推送平台 Android、iOS
    @Column
    private String platform;

    public String getMerNo() {
        return merNo;
    }

    public void setMerNo(String merNo) {
        this.merNo = merNo;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
