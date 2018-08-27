package com.yada.ssp.pushServer.model;

import java.io.Serializable;

public class DevicePK implements Serializable {

    public DevicePK() {
    }

    public DevicePK(String merNo, String loginName) {
        this.merNo = merNo;
        this.loginName = loginName;
    }

    //商户号
    private String merNo;
    //登录名
    private String loginName;

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
}
