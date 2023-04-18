package com.swsm.proxynet.client.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Configuration
@Data
public class ProxyConfig {

    @Value("${proxynet.client.id}")
    private Integer clientId;

    @Value("${proxynet.client.serverIp}")
    private String serverIp;
    @Value("${proxynet.client.serverPort}")
    private Integer serverPort;
    
    
}
