package com.richard.transactionservice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransactionserviceController {
	//TODO to be implemented
	@PostMapping("/account")
    public String createAccount(@RequestBody String request) {
        return "To be implemented. Request: " + request;
    }
	
	//TODO to be implemented
	@PostMapping("/account/transfer")
    public String login(@RequestBody String request) {
        return "To be implemented. Request: " + request;
    }
}
