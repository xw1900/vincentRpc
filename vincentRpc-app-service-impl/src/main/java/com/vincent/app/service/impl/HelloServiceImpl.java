package com.vincent.app.service.impl;

import com.vincent.app.service.HelloService;
import com.vincent.app.service.Person;
import com.vincent.rpc.common.RpcResponse;
import com.vincent.rpc.server.VincentRpcService;

@VincentRpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {
	
	public String hello(String text){
		return "hello rpc " + text;
	}

	@Override
	public Person hello(Person person) {
		Person person2 = new Person();
		person2.setFirstName(person.getLastName());
		person2.setLastName(person.getFirstName());
		return person2;
	}

}
