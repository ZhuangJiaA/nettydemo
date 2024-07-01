package com.sixeco.nettydemo.command;

import cn.hutool.extra.spring.SpringUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CommandHandlerManager {

    private CommandHandlerManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private final ApplicationContext applicationContext;

    private static Map<Integer, Class> handlerMap = new ConcurrentHashMap<>();


    /**
     * 初始化命令处理器
     * @throws Exception
     */
    @PostConstruct
    public void intCommandHandler() throws Exception {
        List<? extends Class<? extends CmdHandler>> collect = SpringUtil.getBeansOfType(CmdHandler.class)
                .values().stream().map(CmdHandler::getClass).toList();
        if (collect.isEmpty()) {
            log.info("没有找到任何命令处理器");
            return;
        }
        for (Class<? extends CmdHandler> aClass : collect) {
            if (Modifier.isAbstract(aClass.getModifiers())) {
                continue;
            }
            /**
             * 强制继续实现方法转换类增加到拦截器中;
             */
            boolean isResolver = CmdHandler.class.isAssignableFrom(aClass);
            if (isResolver) {
                registerCommand(aClass);
            }
        }
    }


    /**
     * 注册命令处理器
     * @param aClass
     * @throws Exception
     */
    private void registerCommand(Class<? extends CmdHandler> aClass) throws Exception {
        CmdHandler cmdHandler;
        try {
            cmdHandler = aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (cmdHandler.getCmd() == null) {
            return;
        }
        //检查创建的功能协议是否有返回功能协议码,是否合理
        int cmdNumber = cmdHandler.getCmd().getNumber();
        ICommand iCommand = Command.forNumber(cmdNumber);
        if (null == iCommand) {
            throw new Exception("failed to register cmd handler, illegal cmd code:" + cmdNumber + ",use Command.addAndGet () to add in the enumerated Command class!");
        }
        Class exClass = handlerMap.get(cmdNumber);
        if (Objects.isNull(exClass)) {
            handlerMap.put(cmdNumber, aClass);
            log.info(" to CmdHandler object by applicationContext cmd[{}] -- Bean Class[{}]", cmdNumber, aClass);
        } else {
            throw new Exception("cmd code:" + cmdNumber + ",-- Bean Class[1。" + exClass + " , 2." + aClass + "]has been registered, please correct!");
        }
    }


    /**
     * 获取命令处理器
     * @param command
     * @return
     */
    public  CmdHandler  getCommand(Integer command){

        if(null == command){
            return null;
        }

        Class<CmdHandler> cmdHandlerClass = handlerMap.get(command);
        if(cmdHandlerClass != null){
            CmdHandler cmdHandler = applicationContext.getBean(cmdHandlerClass);
            return cmdHandler;
        }

        return null;
    }

}
