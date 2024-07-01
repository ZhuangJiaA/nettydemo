package com.sixeco.nettydemo.utils;

/**
 * @author zwj
 * @description: 字节工具类
 */
public class ByteUtlis {
    /**
     * 数组合并
     */
    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    /**
     * 数组转int
     */
    public static  int bytesToInt(byte[] value) {
        return  (value[0]&0xff)<<24|(value[1]&0xff)<<16|(value[2]&0xff)<<8|(value[3]&0xff);
    }
}
