package com.example.ecommerce.config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.ecommerce.model.MyUserDetails;

public class CustomUser implements UserDetails
{

	private MyUserDetails user;
	
	
	public CustomUser(MyUserDetails user) {
		super();
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority authority= new SimpleGrantedAuthority(user.getRole());
		return Arrays.asList(authority);
	}

	@Override
	public String getPassword() {
		
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		
		return user.getEmail();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return user.getAccountNonLocked();
		
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
		
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return user.getIsEnable();
		
	}
	
}
