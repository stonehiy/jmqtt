package org.jmqtt.jmqttapi.api;

import lombok.val;
import lombok.var;
import org.jmqtt.broker.BrokerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApi {

    @Autowired
    private BrokerController brokerController;

    @RequestMapping(path = "test", method = RequestMethod.GET)
    public Object test() {
//        String currentIp = "currentIp";
        String currentIp = brokerController.getCurrentIp();
        return currentIp;
    }

}
