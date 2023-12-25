package com.swsm.proxynet.common.model;

import io.netty.channel.ChannelId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liujie
 * @date 2023-04-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandInfoMessage {
    
    private ChannelId userChannelId;
    private String targetIp;
    private Integer targetPort;
    private byte[] info;
    
    
}
