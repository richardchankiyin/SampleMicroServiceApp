package com.richard.authenticationservice.msg;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.richard.authenticationservice.model.Account;

public class AccountSynchronizerImpl implements AccountSynchronizer {

	private MessagingConnectionFactory factory;
	private RabbitTemplate template;
	
	public AccountSynchronizerImpl() {
		factory = MessagingConnectionFactoryImpl.getInstance();
		this.template = new RabbitTemplate(factory.getConnectionFactory());
	}
	
	@Override
	public void synchronize(Account info) {
		// TODO to be updated with serializing account with unique message key
		//. Also calling dao to update the messaging sending result
		template.convertAndSend("accountsync", info.toString());
	}

}
