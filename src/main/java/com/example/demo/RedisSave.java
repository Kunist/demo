package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class RedisSave {
    List<Integer> list = new ArrayList();
    MultiThreadedGetDemo multiThreadedGetReceiver = new MultiThreadedGetDemo();

    public void saveRedis(String address, List list) throws InterruptedException {
        multiThreadedGetReceiver.run(address, list);
        Thread.currentThread().sleep(1000);
        multiThreadedGetReceiver.run(address, list);



        list.removeAll(list);
    }

}
