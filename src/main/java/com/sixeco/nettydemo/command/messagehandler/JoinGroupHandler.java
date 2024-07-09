package com.sixeco.nettydemo.command.messagehandler;

import com.sixeco.nettydemo.command.AbstractCommandHandler;
import com.sixeco.nettydemo.command.Command;
import com.sixeco.nettydemo.command.ICommand;
import com.sixeco.nettydemo.proto.MessageBody;
import com.sixeco.nettydemo.utils.RedisOperationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JoinGroupHandler extends AbstractCommandHandler {


    @Override
    public ICommand getCmd() {
        return Command.JOIN_GROUP;
    }

    @Override
    public MessageBody.MessageProto handle(MessageBody.MessageProto messageProto) {
        System.out.println("加入小组！");
        RedisOperationUtil.addValue(String.valueOf(messageProto.getId()), messageProto.getJsonData());
        String id = String.valueOf(messageProto.getId()).equals("1") ? "2" : "1";
        String value = RedisOperationUtil.getValue(id);
        MessageBody.MessageProto.Builder builder = MessageBody.MessageProto.newBuilder();
        builder.setJsonData(value);
        return builder.build();
    }
}
