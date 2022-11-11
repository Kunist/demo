package com.example.demo;

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class SnmpUtil {
    private String address="udp:192.160.0.2/161";
    private String username="admin";
    private String authpassword="123";
    private String privpassword="123";
    private Snmp snmp;

    /**
     * 初始化snmp
     * @throws IOException
     */
    public void initSnmp() throws IOException {
        //1、初始化多线程消息转发类
        MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
        //其中要增加三种处理模型。如果snmp初始化使用的是Snmp(TransportMapping<? extends Address> transportMapping) ,就不需要增加
        messageDispatcher.addMessageProcessingModel(new MPv1());
        messageDispatcher.addMessageProcessingModel(new MPv2c());
        //当要支持snmpV3版本时，需要配置user
        OctetString localEngineID=new OctetString(MPv3.createLocalEngineID());
        USM usm=new USM(SecurityProtocols.getInstance().addDefaultProtocols(),localEngineID,0);

        OctetString userName= new OctetString(username);
        OctetString authpass= new OctetString(authpassword);
        OctetString privpass= new OctetString(privpassword);
        UsmUser user= new UsmUser(userName,AuthMD5.ID,authpass,PrivDES.ID,privpass);
        usm.addUser(user.getSecurityName(),user);
        messageDispatcher.addMessageProcessingModel(new MPv3(usm));

        TransportMapping transportMapping= new DefaultUdpTransportMapping();
        snmp = new Snmp(messageDispatcher,transportMapping);
        snmp.listen();
    }

    /**
     * 创建目标对象
     * @param oid
     * @return
     */
    public Target createTarget(String oid){
        Target target=null;
        int version=1;
        if(!(version==SnmpConstants.version1||version==SnmpConstants.version2c||version==SnmpConstants.version3)){
            return target;
        }
        if(version==SnmpConstants.version3){
            target = new UserTarget();
            //snmpV3需要设置安全级别和安全名称，其中安全名称是创建snmp指定user设置的new OctetString("SNMPV3")
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            target.setSecurityName(new OctetString(this.username));
        }else {
            target=new CommunityTarget();
            //snmpV1和snmpV2需要指定团体名名称
            target.setSecurityName(new OctetString(this.username));
            if(version==SnmpConstants.version2c){
                target.setSecurityModel(SecurityModel.SECURITY_MODEL_SNMPv2c);
            }
        }
        target.setVersion(version);
        target.setAddress(GenericAddress.parse(this.address));
        target.setRetries(3);
        target.setTimeout(2000);
        return target;
    }

    /**
     * 配置设备字符串类型的属性，封装成报文添加到PDU中
     * @param pdu
     * @param oid
     * @param var
     */
    public static void setStringVar(PDU pdu,String oid,String var){
        OID oidStr = new OID();
        oidStr.setValue(oid);
        VariableBinding ipBind = new VariableBinding(oidStr,new OctetString(var));
        pdu.add(ipBind);
    }

    /**
     * 配置设备数字类型的属性，封装成报文添加到PDU中
     * @param pdu
     * @param oid
     * @param var
     */
    public static void setIntVar(PDU pdu,String oid,int var){
        OID oidStr = new OID();
        oidStr.setValue(oid);
        VariableBinding ipBind = new VariableBinding(oidStr,new Integer32(var));
        pdu.add(ipBind);
    }
    /**
     * 配置设备数字类型的属性，封装成Guage类型报文添加到PDU中
     * @param pdu
     * @param oid
     * @param var
     */
    public static void setGuage(PDU pdu,String oid,long var){
        OID oidStr = new OID();
        oidStr.setValue(oid);
        VariableBinding ipBind = new VariableBinding(oidStr,new Gauge32(var));
        pdu.add(ipBind);
    }
    public static void setIpAddress(PDU pdu,String oid,String var){
        OID oidStr = new OID();
        oidStr.setValue(oid);
        VariableBinding ipBind = new VariableBinding(oidStr,new IpAddress(var));
        pdu.add(ipBind);
    }

    /**
     * 创建报文
     * @param version
     * @param type
     * @param oid
     * @return
     */
    private static PDU createPDU(int version,int type,String oid){
        PDU pdu=null;
        if(version==SnmpConstants.version3){
            pdu= new ScopedPDU();
        }else {
            pdu= new PDUv1();
        }
        pdu.setType(type);
        //可以添加多个变量oid
        /*for(String oid:oids){
            pdu.add(new VariableBinding(new OID(oid)));
        }*/
        pdu.add(new VariableBinding(new OID(oid)));
        return pdu;
    }

    /**
     * get方式获取属性
     * @param oid
     * @return
     */
    public List<Map> snmpGet(String oid){
        try{
            List<Map> list= new ArrayList<Map>();
            initSnmp();
            Target target = this.createTarget(oid);
            PDU pdu=createPDU(1,PDU.GET,oid);
            ResponseEvent responseEvent = snmp.send(pdu,target);
            PDU response=responseEvent.getResponse();
            if(null==response){
                System.out.println("Timeout.....");
            }else {
                if(response.getErrorStatus()==PDU.noError){
                    Vector<? extends VariableBinding> vbs= response.getVariableBindings();
                    for (VariableBinding  vb: vbs
                    ) {
                        Map map = new HashMap();
                        map.put("value",vb.getVariable());
                        list.add(map);
                    }
                    return list;
                }else {
                    System.out.println("Error:"+response.getErrorStatusText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置属性值
     * @param oid
     * @return
     */
    public  boolean setProprety(String oid) {
        boolean bool = false;
        try{
            initSnmp();
            Target target = this.createTarget(oid);
            PDU pdu=createPDU(1,PDU.SET,oid);
            ResponseEvent responseEvent = snmp.send(pdu, target);
            PDU result = responseEvent.getResponse();
            if(result!=null){
                System.out.println("result:"+result.toString());
                if (result.getErrorStatus() == result.noError) {
                    bool = true;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return bool;
    }

    public   void snmpwalk(String oid){
        try{
            List<Map> list= new ArrayList<Map>();
            initSnmp();
            Target target = this.createTarget(oid);
            PDU pdu=createPDU(1,PDU.GETNEXT,oid);
            boolean matched=true;
            while (matched){
                ResponseEvent responseEvent = snmp.send(pdu,target);
                if(responseEvent==null||responseEvent.getResponse()==null){
                    break;
                }
                PDU response=responseEvent.getResponse();
                String nextOid=null;
                Vector<? extends VariableBinding> vbs= response.getVariableBindings();
                for (int i = 0; i <vbs.size() ; i++) {
                    Map map = new HashMap();
                    VariableBinding vb= vbs.elementAt(i);
                    Variable variable= vb.getVariable();
                    nextOid=vb.getOid().toDottedString();
                    if(!nextOid.startsWith(oid)){
                        matched=false;
                        break;
                    }
                    map.put("oid",nextOid);
                    map.put("value",variable);
                    list.add(map);
                }
                if(!matched){
                    break;
                }
                pdu.clear();
                pdu.add(new VariableBinding(new OID(nextOid)));
            }
        }catch (IOException e){

        }
    }

    //trap
    class TrapReceiver implements CommandResponder{
        //用户名
        private String username = "admin";
        //鉴权密码
        private String authPassword = "123";
        //数据加密密码
        private String privPassword = "123";
        //trap地址
        private String address = "udp:192.168.0.15/162";


        private MultiThreadedMessageDispatcher dispatcher;
        private Snmp snmp = null;
        private Address listenAddress;
        private ThreadPool threadPool;
        private void init() throws UnknownHostException, IOException {
            try {
                //创建接收SnmpTrap的线程池，参数： 线程名称及线程数
                threadPool = ThreadPool.create("Trap", 2);
                //创建一个多线程消息分发器，以同时处理传入的消息，该实例将用于分派传入和传出的消息
                dispatcher = new MultiThreadedMessageDispatcher(threadPool,
                        new MessageDispatcherImpl());
                //监听端的 ip地址 和 监听端口号
                listenAddress = GenericAddress.parse(address);
                //在指定的地址上创建UDP传输
                TransportMapping<?> transport;
                if (listenAddress instanceof UdpAddress) {
                    //必须是本机地址
                    transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);
                } else {
                    transport = new DefaultTcpTransportMapping((TcpAddress) listenAddress);
                }
                //初始化snmp需要设置messageDispatcher里面的参数和TransportMapping参数
                snmp = new Snmp(dispatcher, transport);
                //消息分发器添加接收的版本信息
                /*      v1和v2都具有基本的读、写MIB功能。*
                 *      v2增加了警报、批量数据获取、管理站和管理站通信能力。*
                 *      v3在v2的基础上增加了USM，使用加密的数据和用户验证技术，提高了安全性*/
                snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
                snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
                snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
                //创建具有所提供安全协议支持的USM,//根据本地IP地址和其他四个随机字节创建本地引擎ID
                USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
                SecurityModels.getInstance().addSecurityModel(usm);
                // 添加安全协议,如果没有发过来的消息没有身份认证,可以跳过此段代码
                SecurityProtocols.getInstance().addDefaultProtocols();
                // 创建和添加用户
                OctetString userName1 = new OctetString(username);
                OctetString authPass = new OctetString(authPassword);
                OctetString privPass = new OctetString(privPassword);
                UsmUser usmUser1 = new UsmUser(userName1, AuthMD5.ID, authPass, PrivAES128.ID, privPass);
                //因为接受的Trap可能来自不同的主机，主机的Snmp v3加密认证密码都不一样，所以根据加密的名称，来添加认证信息UsmUser。
                //添加了加密认证信息的便可以接收来自发送端的信息。
                UsmUserEntry userEnty1 = new UsmUserEntry(userName1, usmUser1);
                UsmUserTable userTable = snmp.getUSM().getUserTable();
                // 添加其他用户
                userTable.addUser(userEnty1);
                //开启Snmp监听，可以接收来自Trap端的信息。
                snmp.listen();
                snmp.addCommandResponder(this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                init();
                snmp.addCommandResponder(this);
                System.out.println("开始监听Trap信息!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * 实现CommandResponder的processPdu方法, 用于处理传入的请求、PDU等信息
         * 当接收到trap时，会自动进入这个方法
         *
         * @param respEvnt
         */
        @Override
        public void processPdu(CommandResponderEvent respEvnt) {
            // 解析Response
            System.out.println("trap接受到告警消息，开始对消息进行处理");
            try {
                if (respEvnt != null && respEvnt.getPDU() != null) {
                    PDU pdu=respEvnt.getPDU();
                    Vector<? extends VariableBinding> vbs= pdu.getVariableBindings();
                    for (int i = 0; i <vbs.size() ; i++) {
                        System.out.println("消息体oid:"+vbs.elementAt(i).getOid());
                        System.out.println("消息体oid对应值:"+vbs.elementAt(i).getVariable());
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
