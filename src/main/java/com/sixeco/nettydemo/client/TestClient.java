package com.sixeco.nettydemo.client;

import com.sixeco.nettydemo.handler.MyClientChannelInboundHandler;
import com.sixeco.nettydemo.handler.MyShareCodec;
import com.sixeco.nettydemo.proto.MessageBody;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.sixeco.nettydemo.handler.MyShareCodec.dataRelay;

@Slf4j
public class TestClient extends Thread {

    private final int totalRequests;
    private final long interval; // 每次请求间隔

    public TestClient(int totalRequests, long interval) {
        this.totalRequests = totalRequests;
        this.interval = interval;
    }

    @SneakyThrows
    @Override
    public void run() {
        for(int i = 0; i < totalRequests; i++) {
            // 发起请求
            doRequest();

            // 等待间隔时间
//                Thread.sleep(interval);
        }
    }

    private void doRequest() throws InterruptedException, IOException {
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
                    String data = "There once was a man from Nantucket\n" +
                            "Who kept all his cash in a bucket\n" +
                            "But his daughter, named Nan,\n" +
                            "Ran away with a man\n" +
                            "And as for the money, he...\n" +
                            "\n" +
                            "It was a dark and stormy night when the old mansion suddenly came into view. Thunder rumbled in the distance as I turned down the long driveway, the trees on either side swaying violently in the strong winds. When I reached the heavy wooden doors at the front of the house, I hesitated, not sure whether to knock or run as fast as I could in the other direction. After a few moments debating with myself, I steeled my nerves and rapped loudly three times.\n" +
                            "\n" +
                            "The door creaked open slowly to reveal a pale, thin man peering out at me from within the shadows. \"Can I help you?\" he croaked in a quiet voice. I cleared my throat nervously. \"I received a letter saying you had a room for rent?\" The man nodded slowly. \"Indeed I do. Please, come in out of the storm.\" He swung the door open further to allow me passage. As I crossed the threshold, a shiver ran down my spine that had nothing to do with the howling winds outside. Something wasn't right about this house or its owner, but it was too late to turn back now. I was stuck here for the night, at the mercy of whatever mysteries this ominous mansion held...\n" +
                            "\n" +
                            "The morning sun shone brightly through the bedroom windows, waking me from a restless sleep. Strange dreams of shadowy figures and ghostly whispers had plagued me all night. I stretched and climbed out of the creaky old bed, glancing around the sparse room as the last remnants of sleep faded. All seemed normal now in the light of day. Perhaps the darkness of the previous evening had merely played tricks on my mind. I dressed quickly and made my way downstairs, the old wooden floors groaning under my feet, eager to begin my day and leave this curiously eerie place behind. But would it truly be so easy to escape whatever unknown forces lingered within these walls after night fell once more?";
                    for (int i = 0; i < 1; i++) {
                        MessageBody.MessageProto.Builder messageProto = MessageBody.MessageProto.newBuilder();
                        messageProto.setCmd(2);
                        messageProto.setId(1);
                        messageProto.setName("client1");
                        messageProto.setJsonData(data);
                        MessageBody.MessageProto message = messageProto.build();
                        // 将数据写到输出流
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        message.writeTo(output);
                        System.out.println("data msg length" +  output.toByteArray().length);
                        clientChannel.channel().writeAndFlush(output.toByteArray());
//                        Thread.sleep(300);
                    }
                } else {
                    log.info("异步监听到客户端连接失败,关闭通道和事件组,失败原因: {}", future.cause());
                    dataRelay.clear();
                    clientChannel.channel().close();
                }
            });

        }
    }

    public static void main(String[] args) throws Exception {
        // 每秒10次,每次间隔100ms
        TestClient thread1 = new TestClient(300, 100);
//        TestClient thread2 = new TestClient(500, 100);
        thread1.start();
//        thread2.start();
    }

}
