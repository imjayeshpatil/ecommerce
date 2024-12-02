package com.example.ecommerce.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	
	
	@Override
	public MyUserDetails saveUser(MyUserDetails user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		
		
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		MyUserDetails saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public MyUserDetails getUserByEmail(String email) {
		
		return userRepository.findByEmail(email);
	}

	@Override
	public List<MyUserDetails> getUsers(String role) {
		return	userRepository.findByRole(role);

	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		
		Optional<MyUserDetails> findByuser = userRepository.findById(id);
		
		if(findByuser.isPresent())
		{
			MyUserDetails myUserDetails = findByuser.get();
			myUserDetails.setIsEnable(status);
			userRepository.save(myUserDetails);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(MyUserDetails user) {
		int attempt = user.getFailedAttempt()+1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
		
	}

	@Override
	public void UserAccountLock(MyUserDetails user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);

		
	}

	@Override
	public Boolean unlockAccountTimeExpired(MyUserDetails user) {
		
		long lockTime = user.getLockTime().getTime();
		long unlockTime = lockTime+AppConstant.UNLOCK_DURATION_TIME;
		
		long currentTime = System.currentTimeMillis();
		
		if(unlockTime<currentTime)
		{
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserRestToken(String email, String resetToken) {
		MyUserDetails findByEmail = userRepository.findByEmail(email);
		findByEmail.setResetToken(resetToken);
		userRepository.save(findByEmail);
	}

	@Override
	public MyUserDetails getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	}

	@Override
	public MyUserDetails updateUser(MyUserDetails user) {
		return userRepository.save(user);
	}

	@Override
	public MyUserDetails updateUserProfile(MyUserDetails user,MultipartFile img) {
		MyUserDetails dbUser = userRepository.findById(user.getId()).get();
		
		if(!img.isEmpty()) 
		{
			dbUser.setProfileImage(img.getOriginalFilename());
		}
		
		if(!ObjectUtils.isEmpty(dbUser))
		{
			dbUser.setName(user.getName());
			dbUser.setMobileNo(user.getMobileNo());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser=userRepository.save(dbUser);
			
		}
		
		try {
		if(!img.isEmpty())
		{
			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
					+ img.getOriginalFilename());

			System.out.println(path);
			Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		}}catch(Exception e) {
			e.printStackTrace();
		}
		return dbUser;
	}

	@Override
	public MyUserDetails saveAdmin(MyUserDetails user) {
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		
		
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		MyUserDetails saveUser = userRepository.save(user);
		return saveUser;
		
	}

	
}
