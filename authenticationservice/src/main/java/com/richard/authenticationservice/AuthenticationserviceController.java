package com.richard.authenticationservice;



import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.process.AccountMaintenance;

@RestController
@RequestMapping("/api")
public class AuthenticationserviceController {
	private Logger logger = LoggerFactory.getLogger(AuthenticationserviceController.class);
	private AccountMaintenance accountMaintenance;
	private AuthenticationserviceAppResource resource;
	
	public AuthenticationserviceController() {
		this.resource = AuthenticationserviceAppResourceImpl.getInstance();
		this.accountMaintenance = this.resource.getAccountMaintenance();
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
