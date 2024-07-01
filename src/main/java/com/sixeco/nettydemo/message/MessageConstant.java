package com.sixeco.nettydemo.message;

import lombok.Data;

/**
 * 自己定义的协议消息体
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度             |   数据       |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据
 */
@Data
public class MessageConstant<T> {

    /**
     * 消息的开头的信息标志
     */
    public static byte headData = 0x02;
}
