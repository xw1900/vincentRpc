package com.vincent.rpc.server;

import org.I0Itec.zkclient.ZkClient;

public class ServiceRegistry {

	private ZkClient zkClient;
	// zk地址
	private String registerAddress;
	
	public ServiceRegistry(String registerAddress) {
		this.registerAddress = registerAddress;
		zkClient = new ZkClient(
				registerAddress, 
				Constant.ZK_SESSION_TIMEOUT, 
				Constant.ZK_CONNECTION_TIMEOUT);
	}
	
	public void register(String interfaceName, String serviceAddress) {
		String registryPath = Constant.ZK_REGISTRY_PATH;
		if (!zkClient.exists(registryPath)) {
			zkClient.createPersistent(registryPath);
		}
		
		String servicePath = registryPath + "/" + interfaceName;
		if (!zkClient.exists(servicePath)) {
			zkClient.createPersistent(servicePath);
		}

		String addressPath = servicePath + "/address";
		zkClient.createEphemeralSequential(addressPath, serviceAddress);
	}
}
