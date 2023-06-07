package com.swsm.proxynet.common;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * @author liujie
 * @date 2023-04-15
 */
public class Constants {

    public static final AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");
    public static final AttributeKey<String> VISITOR_ID = AttributeKey.newInstance("visitor_id");
    
    public static final Integer PROXY_MESSAGE_MAX_SIZE = Integer.MAX_VALUE;
    public static final Integer PROXY_MESSAGE_LENGTH_FILED_OFFSET = 0;
    public static final Integer PROXY_MESSAGE_LENGTH_FILED_LENGTH = 4;
    
    public static final Integer READ_IDLE_SECOND_TIME = 3;
    public static final Integer WRITE_IDLE_SECOND_TIME = 3;
    public static final Integer ALL_IDLE_SECOND_TIME = 3;
    
    
    
    public static final Integer PROXY_MESSAGE_TOTAL_SIZE = 4;
    public static final Integer PROXY_MESSAGE_TYPE_SIZE = 1;
    public static final Integer PROXY_MESSAGE_INFO_SIZE = 4;
    
}
