package com.richard.authenticationservice;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthenticationServiceController {
	
	@PostMapping("/createAccount")
    public String createAccount(@RequestBody String accountDetail) {
        return "To be implemented";
    }
	
	@PostMapping("/login")
    public String login(@RequestBody String credential) {
        return "To be implemented";
    }
}
