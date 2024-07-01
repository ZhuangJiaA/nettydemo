package com.sixeco.nettydemo;

import com.sixeco.nettydemo.service.NettyServer;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.InetSocketAddress;

@Slf4j
@SpringBootApplication
public class NettydemoApplication implements CommandLineRunner {

	@Value("${netty.host}")
	private String host;
	@Value("${netty.port}")
	private int port;
	@Autowired
	private NettyServer nettyServer;

	public static void main(String[] args) {
		SpringApplication.run(NettydemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		InetSocketAddress address = new InetSocketAddress(host, port);
		ChannelFuture channelFuture = nettyServer.bind(address);
		log.info("server通道成功建立{}",System.lineSeparator());

		//设置jvm关闭时的钩子函数，去调用通道关闭方法
		Runtime.getRuntime().addShutdownHook(new Thread(() -> nettyServer.destroy()));

		log.info("设置server通道同步等待关闭{}",System.lineSeparator());
		//设置通道同步等待关闭(syncUninterruptibly跟sync的区别为它不会去响应中断事件)
		channelFuture.channel().closeFuture().syncUninterruptibly();
		log.info("server通道已被关闭{}",System.lineSeparator());
	}

}
