package com.richard.authenticationservice;



import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountJDBCTemplate;
import com.richard.authenticationservice.db.AccountSyncDao;
import com.richard.authenticationservice.db.AccountSyncJDBCTemplate;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.msg.AccountSynchronizer;
import com.richard.authenticationservice.msg.AccountSynchronizerImpl;
import com.richard.authenticationservice.msg.MessageKeyGenerator;
import com.richard.authenticationservice.msg.MessageKeyGeneratorImpl;
import com.richard.authenticationservice.msg.MessagingConnectionFactoryImpl;
import com.richard.authenticationservice.process.AccountMaintenance;
import com.richard.authenticationservice.process.AccountMaintenanceImpl;
import com.richard.authenticationservice.process.AccountSequence;
import com.richard.authenticationservice.process.AccountSequenceImpl;

@RestController
@RequestMapping("/api")
public class AuthenticationserviceController {
	private Logger logger = LoggerFactory.getLogger(AuthenticationserviceController.class);
	private AccountMaintenance accountMaintenance;
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	private AccountSynchronizer accountSync;
	private MessageKeyGenerator msgKeyGenerator;
	private AmqpTemplate amqp;
	private AccountSyncDao accountSyncDao;
	
	private AccountSequence createAccountSequence() {
		int maxSequence = 9999; //that is the limit of current sequence impl can support
		LocalDateTime now = LocalDateTime.now();
		// below day1 value should not be changed as accountno format
		// will rely on below minute diff result
		LocalDateTime day1 = LocalDateTime.of(2023, 1, 1, 0, 0);
		long daydiff = ChronoUnit.MINUTES.between(day1, now);
		return new AccountSequenceImpl(maxSequence, daydiff);
	}
	
	public AuthenticationserviceController() {
		this.msgKeyGenerator = new MessageKeyGeneratorImpl();
		this.accountSequence = createAccountSequence();
		this.accountDao = new AccountJDBCTemplate();
		this.accountSyncDao = new AccountSyncJDBCTemplate();
		this.amqp = new RabbitTemplate(MessagingConnectionFactoryImpl.getInstance().getConnectionFactory());
		this.accountSync = new AccountSynchronizerImpl(msgKeyGenerator,amqp,accountSyncDao);
		this.accountMaintenance = new AccountMaintenanceImpl(accountSequence, accountDao, accountSync);
	}
	
	//payload format name=<string>
	//return message with code
	@PostMapping("/createAccount")
    public String createAccount(@RequestBody String accountDetail) {
		logger.info("received request from createAccount: [{}]", accountDetail);
		String input = accountDetail.trim();
		if (input.startsWith("name=")) {
			Account account = new Account();
			account.setName(input.substring(5, input.length()));
			Triplet<Boolean, String, Account> serviceResult = accountMaintenance.createAccount(account);
			if (serviceResult.getValue0()) {
				return serviceResult.getValue1() + "[Account No:" + serviceResult.getValue2().getAccountno() + "]";
			} else {
				return serviceResult.getValue1();
			}
		} else {
			return AuthenticationserviceMessageCode.getInstance().getMessage("E001");
		}
    }
	
	//TODO to be implemented
	@PostMapping("/login")
    public String login(@RequestBody String credential) {
        return "To be implemented. Request: " + credential;
    }
}
