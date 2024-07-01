package com.sixeco.nettydemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author zwj
 * @description: MessageToMessageCodec同时实现了出站编码器和入站解码器，用于同时替代MessageToByteEncoder和ByteToMessageDecoder
 */
@Slf4j
@ChannelHandler.Sharable
public class MyShareCodecProto extends MessageToMessageCodec<ByteBuf, String> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        System.out.println("encode was called......");
        ByteBuf buf = Unpooled.copiedBuffer(s.getBytes());
        list.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        System.out.println("decode was called......");

        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];

        byteBuf.readBytes(bytes);
        String str = new String (bytes);
        list.add(str);
        System.out.println("codec received message: " + str);
    }



}
