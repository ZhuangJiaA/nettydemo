package util;

import java.nio.ByteBuffer;

/**
 * @author ycy
 * @program: netty-dev
 * @description: ByteBuffer工具类
 * @date 2021-12-30 15:53:05
 */
public class ByteBufferUtlis {

    /**
     * 从缓冲区中读取数据
     */
    public static byte[] readByte(ByteBuffer readBuffer) {
        //切换读模式
        readBuffer.flip();
        byte[] returnByte = new byte[readBuffer.remaining()];
        readBuffer.get(returnByte);
        //将缓冲区相关下标重置以备下次读取
        readBuffer.clear();
        return returnByte;
    }
}
