package com.vincent.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.vincent.rpc.common.RpcRequest;
import com.vincent.rpc.common.RpcResponse;

public class VincentRpcProxy {

	private ServiceDiscovery serviceDiscovery;
	
	public VincentRpcProxy(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(final Class<T> interfaceClass){
		
		return (T)Proxy.newProxyInstance(
				interfaceClass.getClassLoader(), 
				new Class[]{interfaceClass}, 
				new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

				RpcRequest request = new RpcRequest();
				request.setRequestId(UUID.randomUUID().toString());
				request.setInterfaceName(method.getDeclaringClass().getName());
				request.setMethodName(method.getName());
				request.setParameterTypes(method.getParameterTypes());
				request.setParameters(params);
				
				String serviceAddress = null;
				// 请求的时候首先得知道地址
				// 此处是在每次请求的时候去调用一次zk
				if (null != serviceDiscovery) {
					String interfaceName = interfaceClass.getName();
					// 通过接口的名字获取服务的地址，如果服务已经发布了。
					serviceAddress = serviceDiscovery.discover(interfaceName);
				}
				
				if (StringUtils.isBlank(serviceAddress)) {
					throw new RuntimeException("木有服务提供。");
				}
				
				String[] address = serviceAddress.split(":");
				String host = address[0];
				int port = Integer.parseInt(address[1]);
				
				VincentRpcClient client = new VincentRpcClient(host, port);
				RpcResponse result = client.send(request);
				
				if (null == result) {
					throw new RuntimeException("result为空");
				}
				
				if (null != result.getException()) {
					throw new RuntimeException(result.getException());
				}
				
				return result.getResult();
			}
		});
	}
}
