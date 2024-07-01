package com.sixeco.nettydemo.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.sixeco.nettydemo.message.MessageData;
import com.sixeco.nettydemo.proto.MessageBody;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;


/**
 * @author zwj
 * @description: 通道业务处理器,SimpleChannelInboundHandler会自动处理释放bytebuf
 */
@ChannelHandler.Sharable
@Slf4j
public class MyClientChannelInboundHandler extends SimpleChannelInboundHandler {

    /**
     * 当 Channel 处于活动状态时被调用；Channel 已经连接/绑定并且已经就绪
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("通道触发channelActive连接事件: {}",ctx.channel());
    }


    /**
     * 当 Channel 离开活动状态并且不再连接它的远程节点时被调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("通道触发channelInactive连接关闭事件: {}",ctx.channel());
        //关闭链路
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }

    /**
     * 当 Channel 从它的 EventLoop 注销并且无法处理任何 I/O 时被调用
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        log.info("通道触发channelUnregistered关闭注册事件: {}",ctx.channel());
        //关闭链路
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }

    /**
     * 当 Channel 的可写状态发生改变时被调用。用户可以确保写操作不会完成
     * 得太快（以避免发生 OutOfMemoryError）或者可以在 Channel 变为再
     * 次可写时恢复写入。可以通过调用 Channel 的 isWritable()方法来检测
     * Channel 的可写性。与可写性相关的阈值可以通过 Channel.config().
     * setWriteHighWaterMark()和 Channel.config().setWriteLowWaterMark()方法来设置
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        log.info("通道触发channelWritabilityChanged可写入事件: {}",ctx.channel());
    }


    /**
     * 当从 Channel 读取数据时被调用
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("通道触发channelRead0事件: {}",ctx.channel());
        if (msg instanceof ByteBuf){
            //如果没有配置对应的ChannelInboundHandlerAdapter去对数据编解码处理的话,最初始传递过来的会是ByteBuf
            ByteBuf buf = (ByteBuf) msg;
            byte[] msgByte = new byte[buf.readableBytes()];
            buf.readBytes(msgByte);
            log.info("[{}]接收到消息: {}{}", DateUtil.date(), StrUtil.str(msgByte,Charset.forName("utf8")),System.lineSeparator());
        }else if (msg instanceof MessageData){
            log.info("[{}]接收到protostuff解码后的消息: {}{}", DateUtil.date(), msg.toString(),System.lineSeparator());
        }else {
            log.info("开始接收响应消息");
            // 接收到流并读取
            ByteArrayInputStream output = new ByteArrayInputStream((byte[]) msg);
            if (output.available() <= 0) {
                log.info("流为空");
            }
            MessageBody.MessageProto messageProto = MessageBody.MessageProto.parseFrom(output);
            log.info("[{}]接收到响应消息: data: {}", DateUtil.date(), messageProto.getJsonData());
        }
    }

    /**
     * 当Channel上的一个读操作完成时被调用
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        log.info("通道触发channelReadComplete事件: {}",ctx.channel());
    }

    /**
     * 通道异常时触发（常用于客户端强迫关闭连接）
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
        log.info("通道触发exceptionCaught异常事件: {}",ctx.channel());
        //关闭链路
        if(ctx.channel().isActive()){
            ctx.close();
        }
    }



}