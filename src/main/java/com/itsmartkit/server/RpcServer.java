package com.itsmartkit.server;

import com.itsmartkit.annotation.RpcService;
import com.itsmartkit.bean.RpcRequest;
import com.itsmartkit.bean.RpcResponse;
import com.itsmartkit.codec.RpcDecoder;
import com.itsmartkit.codec.RpcEncoder;
import com.itsmartkit.common.Constants;
import com.itsmartkit.handler.RpcHandler;
import com.itsmartkit.registry.ServiceRegistry;
import com.itsmartkit.util.IpUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cyj
 * @name RpcServer
 * @description TODO
 * @date 2020/4/7 13:42
 * Version 1.0
 */
@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverHost= "127.0.0.1";

    @Value(value = "${irpc.registry.address}")
    private String registryAddress = "127.0.0.1:2181";

    @Value(value = "${irpc.server.port}")
    private int port = 8800;

    private ServiceRegistry serviceRegistry;

    /*存放接口名与服务对象之间的映射关系*/
    private Map<String, Object> handlerMap = new HashMap<>();

    {
        List<String> localIPList = IpUtil.getLocalIPList();
        int size = localIPList.size();
        if (size > 0) {
            serverHost = localIPList.get(size - 1);
        }
        serviceRegistry = new ServiceRegistry(registryAddress);
    }

    public void afterPropertiesSet() throws Exception {
        new Thread(new NettyServer()).start();
    }

    public class NettyServer implements Runnable {
        @Override
        public void run() {
            EventLoopGroup masterGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                // 创建并初始化 Netty 服务端 Bootstrap 对象
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(masterGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline()
                                        //将RPC请求进行解码（为了处理请求）
                                        .addLast(new RpcDecoder(RpcRequest.class))
                                        //将RPC请求进行编码（为了返回响应）
                                        .addLast(new RpcEncoder(RpcResponse.class))
                                        //处理RPC请求
                                        .addLast(new RpcHandler(handlerMap));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                String host = serverHost;

                //启动RPC服务端
                ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
                LOGGER.info("[irpc]: server started on port: {}", port);

                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                LOGGER.error("[irpc]: server start error", e);
            } finally {
                workerGroup.shutdownGracefully();
                masterGroup.shutdownGracefully();
            }
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(null != serviceBeanMap && serviceBeanMap.size() > 0){
            for (Object serviceBean : serviceBeanMap.values()){
                RpcService annotation = serviceBean.getClass().getAnnotation(RpcService.class);
                Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
                String interfaceName = serviceBean.getClass().getName();
                if (interfaces.length > 0) {
                    interfaceName = interfaces[0].getName();
                }
                if (!StringUtils.isEmpty(annotation.version())) {
                    interfaceName = interfaceName + Constants.VERSION_SEPARATOR + annotation.version();
                }
                handlerMap.put(interfaceName, serviceBean);
                if(null != serviceRegistry){
                    // 注册服务
                    serviceRegistry.register(serverHost + Constants.GROUP_SEPARATOR + port, interfaceName);
                    LOGGER.debug("[irpc]: register service:{}", serverHost + Constants.GROUP_SEPARATOR + port);
                }
            }
        }
    }

}