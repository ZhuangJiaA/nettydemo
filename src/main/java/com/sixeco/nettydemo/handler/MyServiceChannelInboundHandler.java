package com.sixeco.nettydemo.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.sixeco.nettydemo.command.CmdHandler;
import com.sixeco.nettydemo.command.CommandHandlerManager;
import com.sixeco.nettydemo.message.MessageData;
import com.sixeco.nettydemo.proto.MessageBody;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static com.sixeco.nettydemo.handler.MyShareCodec.dataRelay;

/**
 * @author zwj
 * @description: 通道业务处理器，SimpleChannelInboundHandler会自动处理释放butebuf
 */
@ChannelHandler.Sharable
@Slf4j
@Component
public class MyServiceChannelInboundHandler extends SimpleChannelInboundHandler {

    private final CommandHandlerManager commandHandlerManager;

    public MyServiceChannelInboundHandler(CommandHandlerManager commandHandlerManager) {
        this.commandHandlerManager = commandHandlerManager;
    }


    /**
     * 当 Channel 处于活动状态时被调用；Channel 已经连接/绑定并且已经就绪
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("通道触发channelActive连接事件：{}", ctx.channel());
    }

    /**
     * 当 Channel 离开活动状态并且不再连接它的远程节点时被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("通道触发channelInactive断开事件：{}", ctx.channel());
        //关闭通道
        if (ctx.channel().isActive()) {
            dataRelay.clear();
            ctx.close();
        }
    }

    /**
     * 当 Channel 从它的 EventLoop 注销并且无法处理任何 I/O 时被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        log.info("通道触发channelUnregistered注销事件：{}", ctx.channel());
        //关闭通道
        if (ctx.channel().isActive()) {
            dataRelay.clear();
            ctx.close();
        }
    }

    /**
     * 当 Channel 的可写状态发生改变时被调用，用户可以确保写操作不会完成的太快，
     * 以避免发生 OutOfMemoryError 或者可以在 Channel 变为再次可写时恢复写入。
     * 可以通过调用 Channel 的 isWritable() 方法来检测 Channel 的可写性。
     * 于可写性相关的阈值可以通过 Channel.config().setWriteHighWaterMark()
     * 和 Channel.config().setWriteLowWaterSTXMark()方法来设置
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        log.info("通道触发channelWritabilityChanged可写入事件：{}", ctx.channel());
    }

    /**
     * 当从 Channel 读取数据时被调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            //如果没有配置对应的ChannelInboundHandlerAdapter去对数据编码
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] msgByte = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(msgByte);
            log.info("[{}] 接收到消息：{} {}", DateUtil.date(), StrUtil.str(msgByte, Charset.forName("utf-8")), System.lineSeparator());
        } else if (msg instanceof MessageData) {
            log.info("[{}] 接收到protostuff解码后的消息：{} {}",DateUtil.date(), msg, System.lineSeparator());
            //netty所有的io操作都是异步的，所以都会返回一个future对象进行操作，可以用于检查结果是否成功或者获取异常信息，同步等待等操作
            ChannelFuture channelFuture = ctx.channel().writeAndFlush(generateMessage());
            //添加监听器，会在其对应的io事件完成后触发，下面的意思是等发送完（不论发送结果是否成功）之后进行通道关闭
//            channelFuture.addListener(future -> {
//                if (future.isSuccess()) {
//                    log.info("消息发送成功");
//                } else {
//                    log.error("消息发送失败", future.cause());
//                }
//            });
        }else if (msg instanceof String) {
            log.info("开始接收请求消息");
            log.info("[{}]接收到请求消息: {}", DateUtil.date(), msg);
        }else {
            log.info("开始接收请求消息");
            // 接收到流并读取
            ByteArrayInputStream input = new ByteArrayInputStream((byte[]) msg);
            if (input.available() <= 0) {
                log.info("流为空");
            }
            MessageBody.MessageProto messageProto = MessageBody.MessageProto.parseFrom(input);
            log.info("[{}]接收到请求消息: data: {}", DateUtil.date(), messageProto.getJsonData());
            CmdHandler cmdHandler = commandHandlerManager.getCommand(messageProto.getCmd());
            if(cmdHandler == null){
                log.error("onText to  CmdHandler is null by cmd[{}] ", messageProto.getCmd());
            }
            MessageBody.MessageProto response = cmdHandler.handle(messageProto);
            // 将数据写到输出流
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            response.writeTo(output);
            //netty所有的io操作都是异步的，所以都会返回一个future对象进行操作，可以用于检查结果是否成功或者获取异常信息，同步等待等操作
            ChannelFuture channelFuture = ctx.channel().writeAndFlush(output.toByteArray());
        }

    }

    /**
     * 读取完成时触发
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        log.info("通道触发channelReadComplete读取完成事件：{}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.info("通道触发exceptionCaught异常事件：{}", ctx.channel());
        //关闭通道
        if (ctx.channel().isActive()) {
            dataRelay.clear();
            ctx.close();
        }
    }

    /**
     * 生成回复消息
     * @return
     */
    public static MessageData generateMessage() {
        return MessageData.builder().name("server").message("本次消息已查收").build();
    }

}
