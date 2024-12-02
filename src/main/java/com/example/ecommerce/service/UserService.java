package com.example.ecommerce.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.MyUserDetails;

public interface UserService {

	
	public MyUserDetails saveUser(MyUserDetails user);
	
	public MyUserDetails getUserByEmail(String email);
	public List<MyUserDetails> getUsers(String role);

	public Boolean updateAccountStatus(Integer id, Boolean status);
	
	public void increaseFailedAttempt(MyUserDetails user);
	
	public void UserAccountLock(MyUserDetails user);
	
	public Boolean unlockAccountTimeExpired(MyUserDetails user);
	
	public void resetAttempt(int userId);

	public void updateUserRestToken(String email, String resetToken);

	public MyUserDetails getUserByToken(String token);
	
	public MyUserDetails updateUser(MyUserDetails user);
	
	public MyUserDetails updateUserProfile(MyUserDetails user,MultipartFile img);
	
	public MyUserDetails saveAdmin(MyUserDetails user);

}
