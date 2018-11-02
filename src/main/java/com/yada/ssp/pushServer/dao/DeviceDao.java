package com.yada.ssp.pushServer.dao;

import com.yada.ssp.pushServer.model.Device;
import com.yada.ssp.pushServer.model.DevicePK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceDao extends JpaRepository<Device, DevicePK> {

    List<Device> findByMerNo(String merNo);

    List<Device> findByMerNoAndDeviceNo(String merNo, String deviceNo);
}
