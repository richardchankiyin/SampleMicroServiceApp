package com.richard.transactionservice;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.transactionservice.api.AuthenticationValidator;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.process.AccountBalanceMaintenance;

@RestController
@RequestMapping("/api")
public class TransactionserviceController {
	private Logger logger = LoggerFactory.getLogger(TransactionserviceController.class);
	
	private TransactionserviceAppResource appresource;
	private AccountBalanceMaintenance accountBalanceMaintenance;
	private AuthenticationValidator authenticationValidator;
	public TransactionserviceController() {
		this.appresource = TransactionserviceAppResourceImpl.getInstance();
		this.accountBalanceMaintenance = appresource.getAccountBalanceMaintenance();
		this.authenticationValidator = appresource.getAuthenticationValidator();
	}
	
	private String retrieveAccountFromAuthenticationValidator(String value) {
		// sample: [M003]Authorized[accountno=000000000272760001]
		return value.substring(27, value.length()-1);
	}
	
	//payload format sessionkey=<string>
	//return message with code
	@PostMapping("/account")
    public String retrieveBalance(@RequestBody String sessionkey) {
		logger.info("received request from validateSession: [{}]", sessionkey);
		String input = sessionkey.trim();
		if (input.startsWith("sessionkey=")) {
			String sk = input.substring(11, input.length());
			Triplet<Boolean,String,String> authentication = authenticationValidator.authenticate(sk);
			if (authentication.getValue0()) {
				// authorized. Proceed to enquiry
				String accountno = retrieveAccountFromAuthenticationValidator(authentication.getValue1());
				logger.debug("going to enquire balance of account: {}", accountno);
				Triplet<Boolean, String, AccountBalance> balanceResult = accountBalanceMaintenance.enquireBalance(accountno);
				logger.debug("account balance result: {}", balanceResult);
				if (balanceResult.getValue0()) {
					// obtained balance
					return TransactionserviceMessageCode.getInstance().getMessage("M003") + "[balance:" + balanceResult.getValue2().getBalance() + "]";
				} else {
					return TransactionserviceMessageCode.getInstance().getMessage("M002");
				}
			} else {
				logger.debug("authentication result: {}", authentication);
				return TransactionserviceMessageCode.getInstance().getMessage("M002");
			}
		} else {
			return TransactionserviceMessageCode.getInstance().getMessage("M002");
		}
    }
	
	//TODO to be implemented
	@PostMapping("/account/transfer")
    public String transfer(@RequestBody String request) {
        return "To be implemented. Request: " + request;
    }
}
