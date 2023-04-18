package com.swsm.proxynet.server.init;

import com.swsm.proxynet.common.handler.ProxyNetMessageDecoder;
import com.swsm.proxynet.common.handler.ProxyNetMessageEncoder;
import com.swsm.proxynet.server.config.ProxyConfig;
import com.swsm.proxynet.server.handler.ServerClientChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
    
    
    @Override
    public void run(String... args) throws Exception {
        log.info("proxyserver spring启动完成，接下来启动 监听客户端请求的netty服务端");
        
        log.info("启动 监听客户端请求的服务端...");
        ServerBootstrap bootstrapForClient = new ServerBootstrap();
        bootstrapForClient.group(new NioEventLoopGroup(4), new NioEventLoopGroup(8))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ProxyNetMessageDecoder(PROXY_MESSAGE_MAX_SIZE, PROXY_MESSAGE_LENGTH_FILED_OFFSET, PROXY_MESSAGE_LENGTH_FILED_LENGTH));
                        socketChannel.pipeline().addLast(new ProxyNetMessageEncoder());
                        socketChannel.pipeline().addLast(new IdleStateHandler(READ_IDLE_SECOND_TIME, WRITE_IDLE_SECOND_TIME, ALL_IDLE_SECOND_TIME));
                        socketChannel.pipeline().addLast(new ServerClientChannelHandler());
                        
                    }
                });
        try {
            bootstrapForClient.bind(proxyConfig.getClientPort()).sync();
        } catch (Exception e) {
            log.error("启动 监听客户端请求的服务端出现异常", e);
            System.exit(-1);
        }
        log.info("启动 监听客户端请求的服务端 成功...");
    }
}
