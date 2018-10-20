package com.vincent.rpc.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.vincent.rpc.common.RpcRequest;
import com.vincent.rpc.common.RpcResponse;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class VincentRpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private Map<String, Object> handleMap = new HashMap<>();
	public VincentRpcServerHandler(Map<String, Object> handleMap) {
		this.handleMap = handleMap;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("新连接进来了...");
		super.handlerAdded(ctx);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端戳了一下...");
		super.channelActive(ctx);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("客户端好像挂了...");
		super.channelInactive(ctx);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) {
		System.out.println("读到数据了..." + request);
		
		String requestId = request.getRequestId();
		RpcResponse response = new RpcResponse();
		response.setRequestId(requestId);
		
		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Exception e) {
			response.setException(e);
		}
		
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private Object handle(RpcRequest request) throws Exception {
		if (null == request) {
			return "what are you 弄啥呢？";
		}
		
		if (MapUtils.isEmpty(handleMap)) {
			System.out.println("服务端没有服务了...");
			return "服务端没有服务了";
		}
		
		String interfaceName = request.getInterfaceName();
		Object object = handleMap.get(interfaceName);
		if (null == interfaceName) {
			System.out.println("服务端没有服务了...");
			return "服务端没有服务了";
		}
		
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		
		Class<?> interfaceClass = object.getClass();
		Method method = interfaceClass.getMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(object, parameters);
		
	}

}
