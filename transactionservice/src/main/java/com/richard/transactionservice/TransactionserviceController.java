package com.richard.transactionservice;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.richard.transactionservice.api.AuthenticationValidator;
import com.richard.transactionservice.model.AccountBalance;
import com.richard.transactionservice.model.AccountSync;
import com.richard.transactionservice.model.AccountTransfer;
import com.richard.transactionservice.process.AccountBalanceMaintenance;
import com.richard.transactionservice.process.AccountTransferRequestIdGenerator;
import com.richard.transactionservice.process.AdminMonitor;

@RestController
@RequestMapping("/api")
public class TransactionserviceController {
	private Logger logger = LoggerFactory.getLogger(TransactionserviceController.class);
	
	private TransactionserviceAppResource appresource;
	private AccountBalanceMaintenance accountBalanceMaintenance;
	private AuthenticationValidator authenticationValidator;
	private AccountTransferRequestIdGenerator accountTransferRequestIdGenerator;
	private AdminMonitor adminMonitor;
	public TransactionserviceController() {
		this.appresource = TransactionserviceAppResourceImpl.getInstance();
		this.accountBalanceMaintenance = appresource.getAccountBalanceMaintenance();
		this.authenticationValidator = appresource.getAuthenticationValidator();
		this.accountTransferRequestIdGenerator = appresource.getAccountTransferRequestIdGenerator();
		this.adminMonitor = appresource.getAdminMonitor();
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
	

	//payload format sessionkey=<string>,amount=<numeric>
	//return message with code. If success will append balance
	@PostMapping("/account/transfer")
    public String transfer(@RequestBody String request) {
		logger.debug("received request from transfer: [{}]", request);
		String request2 = URLDecoder.decode(request,StandardCharsets.UTF_8);
		String input = request2.trim();
		logger.debug("input: {}", input);
		String[] inputItems = input.split(",");
		try {
			if (inputItems.length != 2) {
				throw new IllegalArgumentException("no delimiter (,) found");				
			}
			if (inputItems[0].startsWith("sessionkey=") && inputItems[1].startsWith("amount=")) {
				String sessionkey = inputItems[0].substring(11, inputItems[0].length());
				String amountStr = inputItems[1].substring(7, inputItems[1].length());
				BigDecimal amount = null;
				try {
					amount = BigDecimal.valueOf(Double.valueOf(amountStr));	
				} catch (Exception e) {
					logger.debug("session key: {} amountStr: {} invalid", sessionkey, amountStr);
					throw new IllegalArgumentException(e);
				}
				
				Triplet<Boolean,String,String> authentication = authenticationValidator.authenticate(sessionkey);
				if (authentication.getValue0()) {
					// authorized, proceed to transfer
					String doneBy = accountTransferRequestIdGenerator.generateUniqueRequestId();
					String accountno = retrieveAccountFromAuthenticationValidator(authentication.getValue1());
					
					
					AccountTransfer accountTransfer = new AccountTransfer();
					accountTransfer.setAccountno(accountno);
					accountTransfer.setAmount(amount);
					accountTransfer.setDoneby(doneBy);

					Triplet<Boolean, String, AccountBalance> result = accountBalanceMaintenance.transfer(accountTransfer);
					logger.debug("transfer result: {}", result);
					if (result.getValue0()) {
						return TransactionserviceMessageCode.getInstance().getMessage("M007") + "[requestid=" + doneBy + ",balance=" + result.getValue2().getBalance() + "]";
					} else {
						return TransactionserviceMessageCode.getInstance().getMessage("M006") + "[requestid=" + doneBy + "]";
					}
					
				} else {
					// not authorized
					return TransactionserviceMessageCode.getInstance().getMessage("M002");
				}				
			} else {
				throw new IllegalArgumentException("cannot capture sessionkey and amount");
			}			
		} catch (IllegalArgumentException ie) {
			logger.error("content invalid", ie);
			return TransactionserviceMessageCode.getInstance().getMessage("M002");
		}
		catch (Exception e) {
			logger.error("severe error found", e);
			return TransactionserviceMessageCode.getInstance().getMessage("M002");
		}
    }
	
	//payload format password=<string>
	//return message A002 is password is correct. Otherwise will 
	//return message M002
	@PostMapping("/admin/checkStatus")
	public String checkStatus(@RequestBody String credential) {
		// use debug mode here because we do not want to show sensitive info when running production!
		logger.debug("received request from checkStatus: [{}]", credential);
		char[] password = credential.substring(9, credential.length()).toCharArray();
		Pair<Boolean, String> result = adminMonitor.checkStatus(password);
		logger.debug("checkStatus result: {}", result);		
		return result.getValue1();
	}
	
	//payload format password=<string>,msgkey=<string>
	//return message A002 is password is correct. Otherwise will 
	//return message M002
	@PostMapping("/admin/retrieveDuplicateAcctSync")
	public String retrieveDuplicateAccountSync(@RequestBody String request) {
		logger.debug("received request from retrieveDuplicateAcctSync: [{}]", request);
		String request2 = URLDecoder.decode(request,StandardCharsets.UTF_8);
		String input = request2.trim();
		logger.debug("input: {}", input);
		String[] inputItems = input.split(",");
		try {
			if (inputItems.length != 2) {
				throw new IllegalArgumentException("no delimiter (,) found");				
			}
			
			if (inputItems[0].startsWith("password=") && inputItems[1].startsWith("msgkey=")) {
				char[] password = inputItems[0].substring(9, inputItems[0].length()).toCharArray();
				String msgkey = inputItems[1].substring(7, inputItems[1].length());
				Triplet<Boolean, String, AccountSync> result = 
						adminMonitor.retrieveDuplicateAccountSync(password, msgkey);
				if (result.getValue0()) {
					AccountSync sync = result.getValue2();
					return result.getValue1() + String.format("[msgkey=%s,accountno=%s,payload=%s,time=%s]"
							, sync.getMsgkey(), sync.getAccountno(), sync.getPayload(), sync.getUptime());
				} else {
					return result.getValue1();
				}
			}
			else {
				throw new IllegalArgumentException("cannot capture password and msgkey");
			}	
		} catch (IllegalArgumentException ie) {
			logger.error("content invalid", ie);
			return TransactionserviceMessageCode.getInstance().getMessage("M002");
		} catch (Exception e) {
			logger.error("severe error found", e);
			return TransactionserviceMessageCode.getInstance().getMessage("M002");
		}
	}
}
