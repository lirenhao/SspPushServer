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
    //终端号
    @Column
    private String termNo;
    //设备号
    @Column
    private String deviceNo;
    //推送方式 FCM、APNS
    @Column
    private String pushType;
    //推送平台 Android、iOS
    @Column
    private String platform;
    //推送标记 0-按终端推送 1-按商户推送
    @Column
    private String pushFlag;

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

    public String getTermNo() {
        return termNo;
    }

    public void setTermNo(String termNo) {
        this.termNo = termNo;
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

    public String getPushFlag() {
        return pushFlag;
    }

    public void setPushFlag(String pushFlag) {
        this.pushFlag = pushFlag;
    }
}
