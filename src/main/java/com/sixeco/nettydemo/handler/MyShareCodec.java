package com.sixeco.nettydemo.handler;

import cn.hutool.core.util.ArrayUtil;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import com.sixeco.nettydemo.message.MessageConstant;
import com.sixeco.nettydemo.message.MessageData;
import com.sixeco.nettydemo.utils.ByteUtlis;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zwj
 * @description: MessageToMessageCodec同时实现了出站编码器和入站解码器，用于同时替代MessageToByteEncoder和ByteToMessageDecoder
 */
@Slf4j
@ChannelHandler.Sharable
public class MyShareCodec extends MessageToMessageCodec<ByteBuf, byte[]> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] s, List<Object> list) throws Exception {
        System.out.println("encode was called......");
        ByteBuf buf = Unpooled.copiedBuffer(s);
        list.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        System.out.println("decode was called......");

        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];

        byteBuf.readBytes(bytes);
//        String str = new String (bytes);
        list.add(bytes);
        System.out.println("codec received message: " + bytes.toString());
    }

}
