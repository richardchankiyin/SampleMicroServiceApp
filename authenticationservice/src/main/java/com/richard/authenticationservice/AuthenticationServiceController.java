package com.richard.authenticationservice;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthenticationServiceController {
	
	//TODO to be implemented
	@PostMapping("/createAccount")
    public String createAccount(@RequestBody String accountDetail) {
        return "To be implemented. Request: " + accountDetail;
    }
	
	//TODO to be implemented
	@PostMapping("/login")
    public String login(@RequestBody String credential) {
        return "To be implemented. Request: " + credential;
    }
}
