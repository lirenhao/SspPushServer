package com.yada.ssp.pushServer.dao;

import com.yada.ssp.pushServer.model.NotifyErr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifyErrDao extends JpaRepository<NotifyErr, String> {

    List<NotifyErr> findByRetryNoLessThanEqualAndDatetimeGreaterThanEqual(int retryNo, String datetime);

    void deleteByNotifydata(String notifydata);
}
