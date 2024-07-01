package com.sixeco.nettydemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author zwj
 * 双向通道处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class MyChannelDuplexHandler extends ChannelDuplexHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("NioServerSocketChannel:" + ctx.channel() + "成功注册到执行器：" + ctx.executor());
        ctx.fireChannelRegistered();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("NioServerSocketChannel:" + ctx.channel() + "成功添加ChannelHandler:" + ctx.handler());
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        log.info("NioServerSocketChannel:" + ctx.channel() + "执行绑定到地址：" + localAddress);
        ctx.bind(localAddress, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("NioServerSocketChannel:" + ctx.channel() + "已激活，准备监听端口事件");
        ctx.fireChannelActive();
    }


}
