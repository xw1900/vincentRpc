package com.vincent.rpc.client;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.I0Itec.zkclient.ZkClient;

public class ServiceDiscovery {

	private String registerAddress;
	private ZkClient zkClient;
	
	public ServiceDiscovery(String registerAddress) {
		this.registerAddress = registerAddress;
	}
	
	// 服务发现可以在客户端启动的时候就全部放到spring容器中，zk挂了也能调。
	public String discover(String interfaceName) {
		String servicePath = Constant.ZK_REGISTRY_PATH + "/" + interfaceName;
		
		zkClient = new ZkClient(
				registerAddress, 
				Constant.ZK_SESSION_TIMEOUT, 
				Constant.ZK_CONNECTION_TIMEOUT);
		
		if (!zkClient.exists(servicePath)) {
			throw new RuntimeException("zk上毛都没有，还调个毛线服务1!");
		}
		
		List<String> addressList = zkClient.getChildren(servicePath);
		if (null == addressList || addressList.isEmpty()) {
			throw new RuntimeException("zk上毛都没有，还调个毛线服务2!");
		}
		
		int addressSize = addressList.size();
		String address = addressList.get(ThreadLocalRandom.current().nextInt(addressSize));
		
		String data = zkClient.readData(servicePath + "/" + address);
		
		// close后下次使用要新建连接，不然connection为空。
		zkClient.close();
		return data;
		
	}
}
