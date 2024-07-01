package com.sixeco.nettydemo.service;

import com.sixeco.nettydemo.handler.MyChannelDuplexHandler;
import com.sixeco.nettydemo.handler.MyServiceChannelInboundHandler;
import com.sixeco.nettydemo.handler.MyShareCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NettyServer {

    private final MyServiceChannelInboundHandler myServiceChannelInboundHandler;

    //创建两个事件组
    //bossGroup 用于接收客户端的连接
    //workerGroup 用于处理客户端的读写操作
    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;

    public NettyServer(MyServiceChannelInboundHandler myServiceChannelInboundHandler) {
        this.myServiceChannelInboundHandler = myServiceChannelInboundHandler;
    }

    public ChannelFuture bind(InetSocketAddress address) {

        //创建业务处理器
//        MyServiceChannelInboundHandler myServiceChannelInboundHandler = new MyServiceChannelInboundHandler();
        //创建共享编解码器
        MyShareCodec shareCodec = new MyShareCodec();
        //用于处理对应的事件
        ChannelDuplexHandler channelDuplexHandler = new MyChannelDuplexHandler();
        //启动器，负责组装netty组件，启动服务器
        ChannelFuture channelFuture = new ServerBootstrap()
                //设置事件组
                .group(bossGroup, workerGroup)
                //创建nio服务通道
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_RCVBUF, 1024)
                .option(ChannelOption.SO_SNDBUF, 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        log.info("接收到一条新的客户端连接：{}",nioSocketChannel);
                        nioSocketChannel.pipeline().addLast(new IdleStateHandler(10, 10, 20, TimeUnit.SECONDS));
                        nioSocketChannel.pipeline().addLast(shareCodec);
                        nioSocketChannel.pipeline().addLast(channelDuplexHandler);
                        nioSocketChannel.pipeline().addLast(myServiceChannelInboundHandler);
                    }
                })
                .bind(address);
        channel = channelFuture.channel();
        log.info("server通道绑定地址及端口号成功,开始设置同步等待{}",System.lineSeparator());
        //设置通道同步等待连接(syncUninterruptibly跟sync的区别为它不会去响应中断事件)
        channelFuture.syncUninterruptibly();
        log.info("设置server通道同步等待连接完成{}",System.lineSeparator());
        return channelFuture;

    }

    /**
     * 关闭通道
     */
    public void destroy() {
        if (null == channel) {
            return;
        }
        log.info("开始关闭server通道{}",System.lineSeparator());
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        log.info("server通道成功关闭{}",System.lineSeparator());
    }

    public Channel getChannel() {
        return channel;
    }

}
