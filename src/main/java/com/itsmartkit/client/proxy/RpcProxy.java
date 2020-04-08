package com.itsmartkit.client.proxy;

import com.itsmartkit.bean.RpcRequest;
import com.itsmartkit.bean.RpcResponse;
import com.itsmartkit.client.RpcClient;
import com.itsmartkit.common.Constants;
import com.itsmartkit.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cyj
 * @name RpcProxy
 * @description TODO
 * @date 2020/4/7 14:41
 * Version 1.0
 */
public class RpcProxy implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serverAddress;

    private AtomicLong atomicLong = new AtomicLong();

    private  String version = "";

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery, String version) {
        this.serviceDiscovery = serviceDiscovery;
        this.version = version;
    }

    public <T> T create(Class<?> interfaceClass){
        //创建动态代理对象


        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                this
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //创建并且初始化RPC请求，并设置请求参数
        RpcRequest request = new RpcRequest();
        request.setRequestId(atomicLong.getAndIncrement());
        String serviceName = method.getDeclaringClass().getName();
        request.setClassName(serviceName);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        if(null != serviceDiscovery){
            //发现服务
            String service = serviceName;
            if (!StringUtils.isEmpty(version)) {
                service = serviceName + Constants.VERSION_SEPARATOR + version;
            }
            serverAddress = serviceDiscovery.discovery(service);
        }

        if(serverAddress == null){
            throw new RuntimeException("serverAddress is null...");
        }

        //解析主机名和端口
        String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        //初始化RPC客户端
        RpcClient client = new RpcClient(host, port);

        long startTime = System.currentTimeMillis();
        //通过RPC客户端发送rpc请求并且获取rpc响应
        RpcResponse response = client.send(request);
        LOGGER.debug("send rpc request elapsed time: {}ms...", System.currentTimeMillis() - startTime);

        if (response == null) {
            throw new RuntimeException("response is null...");
        }

        //返回RPC响应结果
        if(response.hasError()){
            throw response.getError();
        }else {
            return response.getResult();
        }
    }
}