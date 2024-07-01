package com.sixeco.nettydemo.command;

import com.sixeco.nettydemo.proto.MessageBody;

public interface CmdHandler {

    ICommand getCmd();

    MessageBody.MessageProto handle(MessageBody.MessageProto messageProto);

}
