package com.richard.authenticationservice.msg;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

public interface MessagingConnectionFactory {
	public CachingConnectionFactory getConnectionFactory();
}
