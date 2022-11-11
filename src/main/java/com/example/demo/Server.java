package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
//    private float memUsed = 1;
//    private float cpuUsed = 1;
//    private float diskUsed = 1;
//    private float netUsed = 1;
//
//    public Server() {
////        this.memUsed = memUsed;
////        this.cpuUsed = cpuUsed;
////        this.diskUsed = diskUsed;
////        this.netUsed = netUsed;
//    }

    MultiThreadedGetDemo multiThreadedGetReceiver = new MultiThreadedGetDemo();

    List<Integer> list = new ArrayList();

    public String getBestServer() throws InterruptedException {
        String[] backendServers = {
                "202.204.53.13",
                "202.204.53.111",
                "202.204.53.241"
        };  // 上游服务器

//        List<Integer> list = new ArrayList<>();
        Server sv = new Server();

        // 从三台服务器中随机选一台, 判断负载, 若小于50则直接选这台.
//        int index = new Random().nextInt(3);
//        String bestServer = backendServers[index];
        String bestServer = backendServers[0];
        String targetServer = "udp:" + bestServer + "/161";
        double la = getVariable(targetServer);

        if (la < 10) {
            System.out.println(la);
            return bestServer;
        }

        double minLA = 100;
        for (int i = 0; i < backendServers.length; i++) {
            String currentServer = "udp:" + backendServers[i] + "/161";
            double currentLA = getVariable(currentServer);
            System.out.println(currentLA);
            if (currentLA < minLA) {
                bestServer = backendServers[i];
                minLA = currentLA;
            }
        }
        return bestServer;
    }

    public double getVariable(String address) throws InterruptedException {
        multiThreadedGetReceiver.run(address, list);

        // 计算负载和权重
        double memUsed = (float) (list.get(1) * 100) / list.get(0);
        double cpuUsed = 100 - list.get(2);
        double diskUsed = list.get(3);
        double netUsed = 10;
        double la =  0.44 * cpuUsed + 0.22 * memUsed + 0.22 * diskUsed + 0.12 * netUsed;

        list.removeAll(list);
//        System.out.println(la);

        return la;
    }

    public String getBestServerRR() throws InterruptedException {
        String[] backendServers = {
                "202.204.53.13",
                "202.204.53.111",
                "202.204.53.241"
        };  // 上游服务器

        // 从三台服务器中随机选一台
        int index = new Random().nextInt(3);
        String targetServer = backendServers[index];
        return targetServer;
    }
}