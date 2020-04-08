package com.itsmartkit.handler;

import com.itsmartkit.bean.RpcRequest;
import com.itsmartkit.bean.RpcResponse;
import com.itsmartkit.common.Constants;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author cyj
 * @name RpcHandler
 * @description TODO
 * @date 2020/4/7 13:20
 * Version 1.0
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setResponseId(rpcRequest.getRequestId());
        try {
            Object result  = handle(rpcRequest);
            response.setResult(result);
        } catch (Exception ex) {
            LOGGER.error("RpcHandler exception", ex);
            response.setError(ex);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws InvocationTargetException {
        // interfaceName + version
        String className = request.getClassName();
        if (!StringUtils.isEmpty(request.getVersion())) {
            className = request.getClassName() + Constants.VERSION_SEPARATOR + request.getVersion();
        }
        Object serviceBean = handlerMap.get(className);
        if (serviceBean == null) {
            throw new RuntimeException("[irpc]: Class cache not found exception: " + className);
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();

        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod method = fastClass.getMethod(methodName, request.getParameterTypes());
        return method.invoke(serviceBean, request.getParameters());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception...", cause);
        ctx.close();
    }
}