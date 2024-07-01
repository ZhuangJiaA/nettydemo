package com.sixeco.nettydemo.command.messagehandler;

import com.sixeco.nettydemo.command.AbstractCommandHandler;
import com.sixeco.nettydemo.command.Command;
import com.sixeco.nettydemo.command.ICommand;
import com.sixeco.nettydemo.proto.MessageBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GroupSharedResourcesHandler extends AbstractCommandHandler {


    @Override
    public ICommand getCmd() {
        return Command.GROUP_SHARED_RESOURCES;
    }

    @Override
    public MessageBody.MessageProto handle(MessageBody.MessageProto messageProto) {
        System.out.println("小组开始共享资源！");
        return null;
    }
}
