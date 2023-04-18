package com.swsm.proxynet.common.model;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Data
public class ProxyNetMessage {

    public static final byte CONNECT = 0x01;
    public static final byte CONNECT_RESP = 0x02;
    public static final byte COMMAND = 0x03;
    
    public static final String COMMAND_AUTH = "AUTH";
    public static final String COMMAND_INFO = "INFO";
    public static final String COMMAND_RESP = "RESP";

    // 类型
    private byte type;
    // 消息实际信息
    private String info;
    // 用户请求消息 及 目标服务响应消息 原始数据
    private byte[] data;
    
    
    
    public static ProxyNetMessage buildCommandMessage(String type, String message) {
        ProxyNetMessage proxyNetMessage = new ProxyNetMessage();
        proxyNetMessage.setType(COMMAND);
        if (COMMAND_AUTH.equals(type)) {
            proxyNetMessage.setInfo(JSON.toJSONString(new CommandMessage(COMMAND_AUTH, message)));
        } else if (COMMAND_INFO.equals(type)){
            proxyNetMessage.setInfo(JSON.toJSONString(new CommandMessage(COMMAND_INFO, message)));
            proxyNetMessage.setData(JSON.parseObject(message, CommandInfoMessage.class).getInfo());
        } else if (COMMAND_RESP.equals(type)){
            proxyNetMessage.setInfo(JSON.toJSONString(new CommandMessage(COMMAND_RESP, message)));
            proxyNetMessage.setData(JSON.parseObject(message, CommandRespMessage.class).getRespInfo());
        } else {
            throw new RuntimeException("invalid command type:" + type);
        }
        return proxyNetMessage;
    }
    
    public static ProxyNetMessage buildConnectMessage(String userId, String ip, Integer port) {
        ProxyNetMessage proxyNetMessage = new ProxyNetMessage();
        proxyNetMessage.setType(CONNECT);
        proxyNetMessage.setInfo(JSON.toJSONString(new ConnectMessage(userId, ip, port)));
        return proxyNetMessage;
    }

    public static ProxyNetMessage buildConnectRespMessage(String message, Boolean result) {
        ProxyNetMessage proxyNetMessage = new ProxyNetMessage();
        proxyNetMessage.setType(CONNECT_RESP);
        proxyNetMessage.setInfo(JSON.toJSONString(new ConnectRespMessage(result, message)));
        return proxyNetMessage;
    }
    
    
    
}
