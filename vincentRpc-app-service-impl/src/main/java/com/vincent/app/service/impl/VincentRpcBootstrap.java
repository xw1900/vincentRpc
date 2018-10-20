package com.vincent.app.service.impl;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class VincentRpcBootstrap {

	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("spring.xml");
	}
}
