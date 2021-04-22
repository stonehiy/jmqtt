package org.jmqtt.jmqttapi;

import org.jmqtt.jmqttapi.utils.IdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class JmqttApiApplicationTests {

    @Test
    public void contextLoads() {
//        IdWorker idWorker = new IdWorker(31, 31);
//        System.out.println("idWorker=" + idWorker.nextId());
        IdWorker id = new IdWorker();
        long lid = id.nextId();
        System.out.println("id=" + lid);//id=14167960481792

//        System.out.println(id.datacenterId);
//        System.out.println(id.workerId);



    }


}
