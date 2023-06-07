package com.swsm.proxynet.client.init;

import com.swsm.proxynet.client.config.ProxyConfig;
import com.swsm.proxynet.client.handler.ClientServerChannelHandler;
import com.swsm.proxynet.client.handler.ClientTargetChannelHandler;
import com.swsm.proxynet.common.handler.ProxyNetMessageDecoder;
import com.swsm.proxynet.common.handler.ProxyNetMessageEncoder;
import com.swsm.proxynet.common.model.ProxyNetMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static com.swsm.proxynet.common.Constants.ALL_IDLE_SECOND_TIME;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_LENGTH_FILED_LENGTH;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_LENGTH_FILED_OFFSET;
import static com.swsm.proxynet.common.Constants.PROXY_MESSAGE_MAX_SIZE;
import static com.swsm.proxynet.common.Constants.READ_IDLE_SECOND_TIME;
import static com.swsm.proxynet.common.Constants.WRITE_IDLE_SECOND_TIME;

/**
 * @author liujie
 * @date 2023-04-15
 */
@Component
@Slf4j
public class SpringInitRunner implements CommandLineRunner {
    
    @Autowired
    private ProxyConfig proxyConfig;
    
    public static Bootstrap bootstrapForTarget;
    public static Bootstrap bootstrapForServer;
    
    
    @Override
    public void run(String... args) throws Exception {
        log.info("proxyclient spring启动完成，接下来启动 连接代理服务器的客户端");
        
        log.info("启动 连接代理服务器的客户端...");
        bootstrapForServer = new Bootstrap();
        bootstrapForServer.group(new NioEventLoopGroup(4))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ProxyNetMessageDecoder(PROXY_MESSAGE_MAX_SIZE, PROXY_MESSAGE_LENGTH_FILED_OFFSET, PROXY_MESSAGE_LENGTH_FILED_LENGTH));
                        socketChannel.pipeline().addLast(new ProxyNetMessageEncoder());
                        socketChannel.pipeline().addLast(new IdleStateHandler(READ_IDLE_SECOND_TIME, WRITE_IDLE_SECOND_TIME, ALL_IDLE_SECOND_TIME));
                        socketChannel.pipeline().addLast(new ClientServerChannelHandler());
                        
                    }
                });
        bootstrapForServer.connect(proxyConfig.getServerIp(), proxyConfig.getServerPort())
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                log.info("连接代理服务器的客户端 成功...");
                                // 向服务端发送 客户端id信息 
                                future.channel().writeAndFlush(
                                        ProxyNetMessage.buildCommandMessage(ProxyNetMessage.COMMAND_AUTH, 
                                                String.valueOf(proxyConfig.getClientId())));
                            } else {
                                log.info("连接代理服务器的客户端 失败...");
                                System.exit(-1);
                            }
                        }).sync();
        log.info("启动 连接代理服务器的客户端 成功...");

        log.info("初始化 连接被代理服务器的客户端...");
        bootstrapForTarget = new Bootstrap();
        bootstrapForTarget.group(new NioEventLoopGroup(4))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientTargetChannelHandler());
                    }
                });
        log.info("初始化 连接被代理服务器的客户端 完成...");
    }
}
