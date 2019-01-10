package com.yada.ssp.pushServer.dao;

import com.yada.ssp.pushServer.model.Device;
import com.yada.ssp.pushServer.model.DevicePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeviceDao extends JpaRepository<Device, DevicePK> {

    @Query("SELECT d FROM Device d WHERE d.merNo = ?1 AND (d.termNo  = ?2 or d.pushFlag = '1')")
    List<Device> findDevices(String merNo, String TermNo);

    @Query("SELECT d FROM Device d WHERE d.merNo = ?1 AND (d.termNo  = ?2 or d.pushFlag = '1') AND d.deviceNo = ?3")
    List<Device> findDevices(String merNo, String termNo, String deviceNo);
}
