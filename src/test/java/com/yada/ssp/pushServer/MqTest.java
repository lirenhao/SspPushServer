package com.yada.ssp.pushServer;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import org.junit.Test;

import java.io.IOException;

public class MqTest {

    @Test
    public void put() throws MQException, IOException {
        //配置MQ服务器连接参数
        MQEnvironment.hostname = "10.2.53.182";
        MQEnvironment.port = 1470;
        MQEnvironment.channel = "SSP.SVRCONN";
        MQEnvironment.userID = "ssp";

        //设置应用名称,方便服务器MQ 查看应用连接
        MQEnvironment.properties.put(MQConstants.APPNAME_PROPERTY, "MQ Test By Java");

        //创建实例，连接队列管理器
        MQQueueManager queueManager = new MQQueueManager("mqm_ssp");

        //以可写的方式访问队列管理器已定义的队列QUEUE1，当然也可以创建队列
        MQQueue putQueue = queueManager.accessQueue("99", CMQC.MQOO_OUTPUT);

        //新建并发送消息给队列
        String tran = "10412341234123420180827185959                type         1          100  2                                                      1234567890    66666666";
        MQMessage myMessage = new MQMessage();
        myMessage.writeString(tran);

        //使用默认的消息选项
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        //发送消息
        putQueue.put(myMessage, pmo);
        putQueue.close();

        //断开连接
        queueManager.disconnect();
    }

    @Test
    public void get() throws MQException, IOException {
        //配置MQ服务器连接参数
        MQEnvironment.hostname = "10.2.53.182";
        MQEnvironment.port = 1470;
        MQEnvironment.channel = "SSP.SVRCONN";
        MQEnvironment.userID = "ssp";

        //设置应用名称,方便服务器MQ 查看应用连接
        // MQEnvironment.properties.put(MQConstants.APPNAME_PROPERTY, "MQ Test By Java");

        //创建实例，连接队列管理器
        MQQueueManager queueManager = new MQQueueManager("mqm_ssp");

        //以可读的方式访问队列管理器已定义的队列QUEUE1
        MQQueue getQueue = queueManager.accessQueue("99", CMQC.MQOO_INPUT_AS_Q_DEF);

        //从队列读取消息
        MQMessage theMessage = new MQMessage();
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        getQueue.get(theMessage, gmo);

        String object = theMessage.readLine();

        System.out.println(object);
        getQueue.close();

        //断开连接
        queueManager.disconnect();
    }
}
