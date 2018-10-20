package com.vincent.rpc.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vincent.rpc.common.RpcDecoder;
import com.vincent.rpc.common.RpcEncoder;
import com.vincent.rpc.common.RpcRequest;
import com.vincent.rpc.common.RpcResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class VincentRpcServer implements ApplicationContextAware, InitializingBean {

	private String serviceAddress;
	private ServiceRegistry serviceRegistry;

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	// 存放接口名称和接口的实现类，用户客户端请求的时候查找实现类。
	private Map<String, Object> handlerMap = new HashMap<>();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Map<String, Object> beans = context.getBeansWithAnnotation(VincentRpcService.class);
		if (MapUtils.isNotEmpty(beans)) {
			for (Object bean : beans.values()) {
				VincentRpcService vincentRpcService = bean.getClass().getAnnotation(VincentRpcService.class);
				// 获取到注解上面的标识的接口名称
				String interfaceName = vincentRpcService.value().getName();
				handlerMap.put(interfaceName, bean);
			}
		}
	}

	// 服务端启动的时候，上面的方法已经将接口名称和接口的实现类存放到map中了。
	// 接下来要做的就是建立服务端的连接，等待客户端连入了。
	@Override
	public void afterPropertiesSet() throws Exception {
		// 建立连接，采用netty实现
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel sc) throws Exception {
						ChannelPipeline pipeline = sc.pipeline();
//						pipeline.addLast(new StringDecoder());
//						pipeline.addLast(new StringEncoder());
						
						pipeline.addLast(new RpcDecoder(RpcRequest.class));
						pipeline.addLast(new RpcEncoder(RpcResponse.class));
						pipeline.addLast(new VincentRpcServerHandler(handlerMap));
					}
				});

		// 配置文件中的服务地址，就是客户端 以后通过什么地址和服务端通信。
		String[] address = serviceAddress.split(":");
		int port = Integer.parseInt(address[1]);
		ChannelFuture channelFuture = bootstrap.bind(port).sync();

		// 我们暴露接口，得让客户端知道我们的服务在哪里，这里采用zk用户服务注册发现。
		if (null != serviceRegistry) {
			for (String interfaceName : handlerMap.keySet()) {
				serviceRegistry.register(interfaceName, serviceAddress);
			}
		}

		channelFuture.channel().closeFuture().sync();
	}

}
