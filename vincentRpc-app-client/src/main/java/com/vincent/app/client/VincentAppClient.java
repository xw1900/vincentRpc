package com.vincent.app.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vincent.app.service.HelloService;
import com.vincent.app.service.Person;
import com.vincent.rpc.client.VincentRpcProxy;

public class VincentAppClient {

	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		VincentRpcProxy rpcProxy = context.getBean(VincentRpcProxy.class);
		
		HelloService helloService = rpcProxy.create(HelloService.class);
		String string = helloService.hello("xiaowang");
		System.out.println(string);
		
		Person person = new Person();
		person.setFirstName("xiao");
		person.setLastName("wang");
		Person person2 = helloService.hello(person);
		System.out.println(person2);
		
		System.exit(0);
	}
}
