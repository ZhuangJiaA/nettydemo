package com.sixeco.nettydemo.service;

import com.sixeco.nettydemo.proto.MessageBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public interface UserService {

    void bindUserGroup(long userId, long groupId);

    public static byte[] generateProto() throws IOException {
        MessageBody.MessageProto.Builder messageProto = MessageBody.MessageProto.newBuilder();
        messageProto.setCmd(2);
        messageProto.setId(1);
        messageProto.setName("client1");
        messageProto.setJsonData("client1令牌");
        MessageBody.MessageProto message = messageProto.build();
        // 将数据写到输出流
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        // 将数据序列化后发送
        return output.toByteArray();
    }

    public static void main(String[] args) throws IOException {
        MessageBody.MessageProto.Builder messageProto = MessageBody.MessageProto.newBuilder();
        messageProto.setCmd(2);
        messageProto.setId(1);
        messageProto.setName("client1");
        messageProto.setJsonData("client1令牌");
        MessageBody.MessageProto message = messageProto.build();
        // 将数据写到输出流
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        // 获取输出流数据
        byte[] data = output.toByteArray();
        System.out.println(Arrays.toString(data));

//        // 循环遍历每个byte数据
//        String hexString = "";
//        for (byte b : data) {
//            // 转16进制
//            String hex = Integer.toHexString(b & 0xFF);
//            hexString += hex + "";
//
//        }
//        // 输出16进制字符串
//        System.out.println(hexString);
    }

}
