package com.richard.authenticationservice;



import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.authenticationservice.db.AccountDao;
import com.richard.authenticationservice.db.AccountJDBCTemplate;
import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.process.AccountMaintenance;
import com.richard.authenticationservice.process.AccountMaintenanceImpl;
import com.richard.authenticationservice.process.AccountSequence;
import com.richard.authenticationservice.process.AccountSequenceImpl;

@RestController
@RequestMapping("/api")
public class AuthenticationserviceController {
	
	private AccountMaintenance accountMaintenance;
	private AccountSequence accountSequence;
	private AccountDao accountDao;
	
	private AccountSequence createAccountSequence() {
		int maxSequence = 9999; //that is the limit of current sequence impl can support
		LocalDateTime now = LocalDateTime.now();
		// below day1 value should not be changed as accountno format
		// will rely on below day diff result
		LocalDateTime day1 = LocalDateTime.of(2023, 1, 1, 0, 0);
		long daydiff = ChronoUnit.MINUTES.between(day1, now);
		return new AccountSequenceImpl(maxSequence, daydiff);
	}
	
	public AuthenticationserviceController() {
		this.accountSequence = createAccountSequence();
		this.accountDao = new AccountJDBCTemplate();
		this.accountMaintenance = new AccountMaintenanceImpl(accountSequence, accountDao);
	}
	
	//payload format name=<string>
	@PostMapping("/createAccount")
    public String createAccount(@RequestBody String accountDetail) {
		String input = accountDetail.trim();
		if (input.startsWith("name=")) {
			Account account = new Account();
			account.setName(input.substring(5, input.length()));
			String result = accountMaintenance.createAccount(account).getValue1();
			//TODO to be changed to json message
			return result;
		} else {
			//TODO to be changed to json message
			return "not accepted";
		}
    }
	
	//TODO to be implemented
	@PostMapping("/login")
    public String login(@RequestBody String credential) {
        return "To be implemented. Request: " + credential;
    }
}
