package com.swsm.proxynet.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author liujie
 * @date 2023-04-15
 */
@SpringBootApplication
@Slf4j
@ComponentScan(value = {"com.swsm.proxynet.*"})
public class ProxynetClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxynetClientApplication.class, args);
    }
    
}
