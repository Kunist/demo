package com.example.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @description: get方式获取数据
 * @author: yghhz
 * @create: 2020-11-15 10:14
 **/
public class MultiThreadedGetDemo {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger LOGGER = LogManager.getLogger(MultiThreadedGetDemo.class);
//    //用户名
//    private String username = "root123123";
//    //鉴权密码
//    private String authPassword = "123123123";
//    //数据加密密码
//    private String privPassword = "hello123";
//    //trap地址
//    private String address = "udp:10.23.140.255/162";
    //get 地址
//    private String address = "udp:202.204.53.241/161";

    private MultiThreadedMessageDispatcher dispatcher;
    private Snmp snmp = null;
//    private static List<Integer> list = new ArrayList();

    public MultiThreadedGetDemo() {

    }

    public void  initSnmp() throws IOException {
        //1、初始化多线程消息转发类
        MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
        //其中要增加三种处理模型。如果snmp初始化使用的是Snmp(TransportMapping<? extends Address> transportMapping) ,就不需要增加
//        messageDispatcher.addMessageProcessingModel(new MPv1());
        messageDispatcher.addMessageProcessingModel(new MPv2c());
//        //当要支持snmpV3版本时，需要配置user
//        OctetString localEngineID = new OctetString(MPv3.createLocalEngineID());
//        USM usm = new USM(SecurityProtocols.getInstance().addDefaultProtocols(), localEngineID, 0);
//
//        OctetString userName1 = new OctetString(username);
//        OctetString authPass = new OctetString(authPassword);
//        OctetString privPass = new OctetString(privPassword);
//        UsmUser user = new UsmUser(userName1, AuthMD5.ID, authPass, PrivAES128.ID, privPass);
//
//        usm.addUser(user.getSecurityName(), user);
//        messageDispatcher.addMessageProcessingModel(new MPv3(usm));

        //2、创建transportMapping  ip为本地ip，可以不设置
        TransportMapping<?> transportMapping = new DefaultUdpTransportMapping();
        //3、正式创建snmp
        snmp = new Snmp(messageDispatcher, transportMapping);
        //开启监听
        snmp.listen();
    }

    Target createTarget(String address) {
        Target target = new CommunityTarget();
        int version = SnmpConstants.version2c;
        OctetString community = new OctetString("public");
        ((CommunityTarget) target).setCommunity(community);
        target.setSecurityModel(SecurityModel.SECURITY_MODEL_SNMPv2c);
//        int version = 1;
//        if (!(version == SnmpConstants.version3 || version == SnmpConstants.version2c || version == SnmpConstants.version1)) {
//            //log.error("参数version异常");
//            return target;
//        }
//        if (version == SnmpConstants.version3) {
//            target = new UserTarget();
//            //snmpV3需要设置安全级别和安全名称，其中安全名称是创建snmp指定user设置的new OctetString("SNMPV3")
//            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
//            target.setSecurityName(new OctetString(this.username));
//        } else {
//            //snmpV1和snmpV2需要指定团体名名称
//            target = new CommunityTarget();
//            ((CommunityTarget) target).setCommunity(new OctetString(this.username));
//            if (version == SnmpConstants.version2c) {
//                target.setSecurityModel(SecurityModel.SECURITY_MODEL_SNMPv2c);
//            }
//        }
        target.setVersion(version);
        target.setAddress(GenericAddress.parse(address));
        target.setRetries(3);
        target.setTimeout(2000);
        return target;
    }

    static PDU createPDU(int type, String oid) {
        PDU pdu = new PDU();
        pdu.setType(type);
        pdu.add(new VariableBinding(new OID(oid)));
        //可以添加多个变量oid
//        for(String oid : oids){
//            pdu.add(new VariableBinding(new OID(oid)));
//        }
//        pdu.add(new VariableBinding(new OID(oid)));
        return pdu;
    }


//    public  List<Map> snmpGet(String[] oids) {
//        try {
////            LOGGER.info("get方式");
//            List<Map> list = new ArrayList<>();
//            //1、初始化snmp,并开启监听
//            initSnmp();
//            //2、创建目标对象
//            Target target = this.createTarget();
//            //3、创建报文
//            int type = 0x1;
//            PDU pdu = createPDU(, oids);
//            //4、发送报文，并获取返回结果
//            ResponseEvent responseEvent = snmp.send(pdu, target);
//            PDU response = responseEvent.getResponse();
////            System.out.println(response);
//            if (response == null) {
//                System.out.println("TimeOut...");
//            } else {
//                if (response.getErrorStatus() == PDU.noError) {
//                    //get方式获取到的返回值
//                    Vector<? extends VariableBinding> vbs = response.getVariableBindings();
//                    for (VariableBinding vb : vbs) {
//                        Map map = new HashMap();
//                        map.put("value",vb.getVariable());
//                        System.out.println("OID:"  + vb.getVariable());
////                        LOGGER.info("OIDvALUE"  + vb.getVariable());
//                        list.add(map);
//                    }
//                    return list;
//                } else {
//                    System.out.println("Error:" + response.getErrorStatusText());
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    //开启监控的main方法。
//    public static void main(String[] args) throws InterruptedException {
//        MultiThreadedGetDemo multithreadedtrapreceiver = new MultiThreadedGetDemo();
//
//        while (true) {
//            multithreadedtrapreceiver.run();
//            System.out.println(list);
//            list.removeAll(list);
//            Thread.sleep(2000);
//        }
//    }

    public void run(String address, List<Integer> list) {
        try {
            String[] devOids = {
                    "1.3.6.1.2.1.25.2.2.0", // 内存
                    "1.3.6.1.4.1.2021.4.6.0", // 已使用内存
                    "1.3.6.1.4.1.2021.11.11.0",  // CPU空闲率
                    "1.3.6.1.4.1.2021.13.15.1.1.9.4",  // 一分钟内磁盘io负载率
                    "1.3.6.1.2.1.2.2.1.10.2", // 发送数据 字节数
                    "1.3.6.1.2.1.2.2.1.16.2", // 接收数据 字节数

            };

            //1、初始化snmp,并开启监听
            initSnmp();
            //2、创建目标对象
            Target target = this.createTarget(address);
            for (int i = 0; i < devOids.length; i++) {
                String oid = devOids[i];

                //3、创建报文, 判断使用哪种方式获取
                int type = i > 2 ? PDU.GETNEXT : PDU.GET;
                PDU pdu = createPDU(type, oid);
                //4、发送报文，并获取返回结果
                ResponseEvent responseEvent = snmp.send(pdu, target);
                PDU response = responseEvent.getResponse();
                if (response == null) {
                    System.out.println("TimeOut...");
                } else {
                    if (response.getErrorStatus() == PDU.noError) {
                        //get方式获取到的返回值
                        Vector<? extends VariableBinding> vbs = response.getVariableBindings();
                        Variable vb = vbs.elementAt(0).getVariable();
//                        System.out.println(vb);
//                        redisTemplate.opsForList().rightPush("node", vb);
                        list.add(Integer.parseInt(vb.toString()));
                    } else {
                        System.out.println("Error:" + response.getErrorStatusText());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


