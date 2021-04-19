package org.jmqtt.jmqttapi;

import org.jmqtt.broker.BrokerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JmqttApiApplication implements ApplicationRunner {


    @Autowired
    private BrokerController brokerController;


    public static void main(String[] args) {
        SpringApplication.run(JmqttApiApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        brokerController.start();

    }
}
