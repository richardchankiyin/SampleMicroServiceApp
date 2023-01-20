package com.richard.authenticationservice.msg;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class MessagingConnectionFactoryImpl implements MessagingConnectionFactory{
	
	private CachingConnectionFactory factory;
	
	public MessagingConnectionFactoryImpl() {
		// context cannot be closed here otherwise the factory created cannot create connection when necessary
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("messagingconnectionfactory.xml");
		this.factory = (CachingConnectionFactory)context.getBean("messagingconnectionfactory");
	}
	
	@Override
	public CachingConnectionFactory getConnectionFactory() {
		return factory;
	}
	
	private static MessagingConnectionFactoryImpl instance = new MessagingConnectionFactoryImpl();
	public static MessagingConnectionFactoryImpl getInstance() {
		return instance;
	}
}
