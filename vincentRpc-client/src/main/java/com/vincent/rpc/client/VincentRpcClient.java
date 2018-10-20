package com.vincent.rpc.client;

import com.vincent.rpc.common.RpcDecoder;
import com.vincent.rpc.common.RpcEncoder;
import com.vincent.rpc.common.RpcRequest;
import com.vincent.rpc.common.RpcResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class VincentRpcClient extends SimpleChannelInboundHandler<RpcResponse> {

	private String host;
	private int port;
	
	private RpcResponse result;
		
	public VincentRpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public RpcResponse send(RpcRequest request) throws Exception {
		
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
		.channel(NioSocketChannel.class)
		.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel sc) throws Exception {
				ChannelPipeline pipeline = sc.pipeline();
//				pipeline.addLast(new StringEncoder()); //编码
//				pipeline.addLast(new StringDecoder()); //解码
				
				pipeline.addLast(new RpcDecoder(RpcResponse.class));
				pipeline.addLast(new RpcEncoder(RpcRequest.class));
				pipeline.addLast(VincentRpcClient.this); //in
			}
		});
		
		ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
		Channel channel = channelFuture.channel();
		
		channel.writeAndFlush(request).sync();
		
		// 等待服务器关闭通道
		channel.closeFuture().sync();
		
		return result;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse result) throws Exception {
		System.out.println("客户端收到了数据--" + result);
		this.result = result;
	}
}
