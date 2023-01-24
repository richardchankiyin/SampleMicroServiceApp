package com.richard.authenticationservice;



import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.authenticationservice.model.Account;
import com.richard.authenticationservice.model.AccountLoginSession;
import com.richard.authenticationservice.process.AccountLogin;
import com.richard.authenticationservice.process.AccountMaintenance;

@RestController
@RequestMapping("/api")
public class AuthenticationserviceController {
	private Logger logger = LoggerFactory.getLogger(AuthenticationserviceController.class);
	private AccountMaintenance accountMaintenance;
	private AccountLogin accountLogin;
	private AuthenticationserviceAppResource resource;
	
	public AuthenticationserviceController() {
		this.resource = AuthenticationserviceAppResourceImpl.getInstance();
		this.accountMaintenance = this.resource.getAccountMaintenance();
		this.accountLogin = this.resource.getAccountLogin();
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

	@PostMapping("/login")
    public String login(@RequestBody String credential) {
		// use debug mode here because we do not want to show sensitive info when running production!
		logger.debug("received request from login: [{}]", credential);
		String input = credential.trim();
		String[] inputItems = input.split(",");
		try {
			// TODO parsing logic to be changed. Need to handle escape characters
			if (inputItems.length != 2) {
				throw new IllegalArgumentException("no delimiter (,) found");
			}
			if (inputItems[0].startsWith("accountno=") && inputItems[1].startsWith("password=")) {
				String accountnocontent = inputItems[0];
				String passwordcontent = inputItems[1];
				
				String account = accountnocontent.substring(10, accountnocontent.length());
				char[] password = passwordcontent.substring(9, passwordcontent.length()).toCharArray();
				
				Triplet<Boolean, String, AccountLoginSession> result = accountLogin.login(account, password);
				
				if (result.getValue0()) {
					// login successfully
					return result.getValue1() + "[Session:" + result.getValue2().getSessionkey() +"]";
					
				} else {
					// failed to login
					return result.getValue1();
				}
				
			} else {
				throw new IllegalArgumentException("cannot capture accountno and password");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("content invalid", ie);
			return AuthenticationserviceMessageCode.getInstance().getMessage("E004");
		} catch (Exception e) {
			logger.error("severe error found!", e);
			return AuthenticationserviceMessageCode.getInstance().getMessage("E004");
		}

    }
}
