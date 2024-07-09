package com.sixeco.nettydemo.handler;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.sixeco.nettydemo.message.MessageConstant;
import com.sixeco.nettydemo.utils.ByteUtlis;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.TooLongFrameException;
import lombok.extern.slf4j.Slf4j;
import util.ByteBufferUtlis;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zwj
 * @description: MessageToMessageCodec同时实现了出站编码器和入站解码器，用于同时替代MessageToByteEncoder和ByteToMessageDecoder
 */
@Slf4j
@ChannelHandler.Sharable
public class MyShareCodecString extends MessageToMessageCodec<ByteBuf, String> {

    private Class<?> genericClass;

    /**
     * 消息的开头的信息标志
     */
    public static byte headData = 0x02;

    /**
     * 协议开始的标准headData，byte类型，占据1个字节.
     * 表示数据的长度contentLength，int类型，占据4个字节.
     * 总共是5个字节的长度描述消息前缀
     */
    public static final int BASE_LENGTH = 5;

    /**
     * 允许传输的最大长度
     */
    public static final int MAX_LENGTH = 1024000;

    /**
     * @describe 数据中转存储，实际用的时候记得要写定时器定时检查通道是不是已经关闭去进行数据清除。
     * @return
     */
    public  static Map<Channel, byte[]> dataRelay = new ConcurrentHashMap<>();


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 1024);
        // 先写入消息起始标识
        readBuffer.put(headData);
        // 写入消息长度
        int length = s.length();
        readBuffer.putInt(length);
        // 写入消息内容
        readBuffer.put(s.getBytes());
        byte[] bytes = ByteBufferUtlis.readByte(readBuffer);
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        list.add(buf);
        log.info("encode was called...... " + s.length() + " " + bytes.length);
    }

//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        log.info("decode was called......  " + in.readableBytes());
//        //记录本次报文的数据长度
//        int dataLength;
//        while (in.readableBytes() > 0) {
//            log.info("decode was called......  " + in.readableBytes());
//            // 标记当前可读位置下标用于重置可读下标
//            in.markReaderIndex();
//
//            //开始读取第一个字节,检查是否为报文开始标识
//            if (in.readByte() != headData) {
//                log.info("检测到本次首字节不为开始标识,直接跳过本字节内容{}", System.lineSeparator());
//                //不是的话略过该字节读取下一个字节
//                continue;
//            }
//
//            //读取到包头后,开始读取长度标识
//            dataLength = in.readInt();
//            //如果内容长度为0,则表示本报文无具体数据,直接进行返回
//            if (dataLength == 0) {
//                return;
//            }
//
//            //检查可读的字节是否已经满足数据长度
//            if (in.readableBytes() < dataLength) {
//                //不满足的话重置可读位置,等待下次数据读取
//                in.resetReaderIndex();
//                log.info("检测到本次可读的字节「{}」小于数据长度,重置可读位点不做任何操作{}", in.readableBytes(), System.lineSeparator());
//                return;
//            }
//
//            //如果需要随机读写的话可以使用get和set方法，并不会改变读写指针
//            //读取指定长度的数据
//            byte[] outData = new byte[dataLength];
//            in.readBytes(outData);
//            // 读取到数据后，将数据写入out集合中，以便下一个handler处理
//            out.add(outData);
//            log.info("codec received message: {}", Arrays.toString(outData));
//
//            //重用bytebuffer中已读的内存空间，防止动态扩张
//            //该操作会将待读取的数据左移复用已读的缓冲内存空间，因此会发生字节数组的内存复制，如果频繁调用可能会导致牺牲了cpu的性能来换取更多的内存。
//            in.discardReadBytes();
//        }
//        System.out.println("结束后的长度：" + in.readInt());
//
//    }


    /**
     * @return
     * @describe 对字节数据进行解码。底层会通过acceptInboundMessage方法自动拿到你的泛型类型去判断传入的数据是否需要进行解码处理
     * @describe 跟实现ByteToMessageDecoder的decode方法不同的是，MessageToMessageCodec的decode方法在数据读取后要进行存储，不然可能会丢失。（应该是底层的自动释放操作影响的）
     * @Param: ctx 连接通道
     * @Param: in 字节数据缓冲区，多次传输过程中用的是同一个bytebuf
     * @Param: out 往下传递的解码数据，如果out中存在多个对象的话会一个个往下传递处理，而不是一次性传递多个
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) {
        // ByteBuf的hasArray()可以用于鉴别是否为堆内内存(为true)还是堆外内存分配(为false);
        // 如果ByteBuf分配到的是堆外内存的话，是不允许直接使用array()方法直接获取到数组数据的，会报错
        log.info("数据类型{}{}", in.getClass().getName(), System.lineSeparator());

        if (in.readableBytes() > MAX_LENGTH) {
            //如果发现超出大小，则抛出TooLongFrameException异常跳过所有的可读字节。通知ChannelHandler的exceptionCaught进行异常处理
            throw new TooLongFrameException("Frame too big!");
        }

        Integer beginIndex = null;
        if (in.readableBytes() > 0) {
            byte[] beforeByteData = dataRelay.get(ctx.channel());
            byte[] currentByteData = new byte[in.readableBytes()];
            in.readBytes(currentByteData);

            if (ObjectUtil.isNotNull(beforeByteData)) {
                currentByteData = ByteUtlis.byteMergerAll(beforeByteData, currentByteData);
            }

            while (true) {
                //找寻开始标识
                for (int i = 0; i < currentByteData.length; i++) {
                    if (currentByteData[i] == MessageConstant.headData) {
                        log.info("检测开始标识{}", System.lineSeparator());
                        beginIndex = i;
                        break;
                    }
                }
                if (ObjectUtil.isNull(beginIndex)) {
                    return;
                }


                //检查是否满足可读的最小界限
                if (currentByteData.length - beginIndex < BASE_LENGTH) {
                    //存放到数据中转中等下一次读取
                    dataRelay.put(ctx.channel(), ArrayUtil.sub(currentByteData, beginIndex, currentByteData.length));
                    return;
                }

                byte[] lengthByte = ArrayUtil.sub(currentByteData, beginIndex + 1, beginIndex + 5);
                //获取数据长度
                int length = ByteUtlis.bytesToInt(lengthByte);

                //检查是否已经满足数据长度读取
                if (currentByteData.length - beginIndex < BASE_LENGTH + length) {
                    //存放到数据中转中等下一次读取
                    dataRelay.put(ctx.channel(), ArrayUtil.sub(currentByteData, beginIndex, currentByteData.length));
                    return;
                }

                //截取对应长度的数据进行解码数据进行解码
                byte[] outData = ArrayUtil.sub(currentByteData, beginIndex + BASE_LENGTH, beginIndex + BASE_LENGTH + length);
                //往下传递数据
                out.add(outData);

                //检查是否还有剩余数据需要留到下一次读取报文
                if (currentByteData.length > beginIndex + BASE_LENGTH + length + 1) {
                    currentByteData = ArrayUtil.sub(currentByteData, beginIndex + BASE_LENGTH + length, currentByteData.length);
                    dataRelay.put(ctx.channel(), currentByteData);
                    //重置开始标识用于下一轮读取
                    beginIndex = null;
                } else {
                    //已经读取完的话直接删除调该通道中转数据
                    dataRelay.remove(ctx.channel());
                    return;
                }
            }

        }
    }

}
