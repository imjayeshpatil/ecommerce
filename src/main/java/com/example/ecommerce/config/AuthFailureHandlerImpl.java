package com.example.ecommerce.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {
	
	@Autowired
	private UserRepository userRepository;
	
	@Lazy
	@Autowired
	private UserService userService;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String email = request.getParameter("username");

		MyUserDetails myUserDetails = userRepository.findByEmail(email);

		if(myUserDetails!=null){
			
		
		if ( myUserDetails.getIsEnable()) {

			if (myUserDetails.getAccountNonLocked()) {

				if (myUserDetails.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
					userService.increaseFailedAttempt(myUserDetails);
				} else {
					userService.UserAccountLock(myUserDetails);
					exception = new LockedException("Your account is locked !! failed attempt 3");
				}
			} else {

				if (userService.unlockAccountTimeExpired(myUserDetails)) {
					exception = new LockedException("Your account is unlocked !! Please try to login");
				} else {
					exception = new LockedException("your account is Locked !! Please try after sometimes");
				}
			}

		} else {
			exception = new LockedException("your account is inactive");
		}
	}else {
		exception = new LockedException("Email & password is invalid");
	}
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

	
	
}
