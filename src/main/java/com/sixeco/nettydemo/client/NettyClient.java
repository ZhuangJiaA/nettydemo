package com.sixeco.nettydemo.client;

import com.sixeco.nettydemo.handler.MyClientChannelInboundHandler;
import com.sixeco.nettydemo.handler.MyShareCodec;
import com.sixeco.nettydemo.message.MessageData;
import com.sixeco.nettydemo.proto.MessageBody;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
public class NettyClient {

    public static void main(String[] args) throws InterruptedException, IOException {

        //是否需要同步操作
        boolean sync = false;

        NioEventLoopGroup group = new NioEventLoopGroup();
        //创建共享编解码器
        MyShareCodec shareDecoder = new MyShareCodec();
        //创建业务处理器
        MyClientChannelInboundHandler myClientChannelInboundHandler = new MyClientChannelInboundHandler();

        ChannelFuture clientChannel = new Bootstrap()
                //设置事件组
                .group(group)
                //设置对应类型的通道
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1024)
                .option(ChannelOption.SO_SNDBUF, 1024)
                .handler(new ChannelInitializer<Channel>() {
                    //channel 代表和客户端进行数据读写的通道 Initializer 初始化，负责添加别的 handler
                    //在每个连接建立后会去调用该初始化方法
                    @Override
                    protected void initChannel(Channel ch) {
                        //消息编解码处理器（可以共享，多个连接共用同一个实例）
                        ch.pipeline().addLast(shareDecoder);

                        ch.pipeline().addLast(myClientChannelInboundHandler);
                    }
                })
                //设置连接的端口
                .connect("127.0.0.1", 8080);

        if (sync) {
            //同步等待连接成功
            clientChannel.sync();
            log.info("同步等待到客户端连接成功,开始发送数据");
            //获取通道对象进行写操作
            clientChannel.channel().writeAndFlush(NettyClient.generateProto());
        } else {
            //异步监听连接成功/失败事件
            clientChannel.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("异步监听到客户端连接成功,开始发送数据");
                    //获取通道对象进行写操作
                    clientChannel.channel().writeAndFlush(NettyClient.generateProto());
                } else {
                    log.info("异步监听到客户端连接失败,关闭通道和事件组,失败原因: {}", future.cause());
                    clientChannel.channel().close();
                }
            });

        }
    }

    /**
     * 随机根据指定格式生成消息
     */
    public static MessageData generateMessage() {
        StringJoiner str = new StringJoiner("-");
        for (int i = 0; i < 1; i++) {
            str.add(UUID.randomUUID().toString());
        }
        MessageData data = new MessageData();
        data.setName("client");
        data.setMessage(str.toString());
        return data;
    }

    public static byte[] generateProto() throws IOException {
        MessageBody.MessageProto.Builder messageProto = MessageBody.MessageProto.newBuilder();
        messageProto.setCmd(2);
        messageProto.setId(1);
        messageProto.setName("zwj");
        messageProto.setJsonData("ZWJ申请加入！");
        MessageBody.MessageProto message = messageProto.build();
        // 将数据写到输出流
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        // 将数据序列化后发送
        return output.toByteArray();
    }

}
