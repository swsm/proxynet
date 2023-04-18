package com.swsm.proxynet.server;

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
public class ProxynetServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxynetServerApplication.class, args);
    }
    
}
