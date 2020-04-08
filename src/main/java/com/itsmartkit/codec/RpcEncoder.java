package com.itsmartkit.codec;

import com.itsmartkit.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author cyj
 * @name RpcEncoder
 * @description TODO RPC请求编码，只需扩展 Netty 的MessageToByteEncoder抽象类，并且实现其encode方法即可
 * @date 2020/4/7 14:34
 * Version 1.0
 */
public class RpcEncoder extends MessageToByteEncoder{

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if(genericClass.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}