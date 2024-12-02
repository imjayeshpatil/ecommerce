package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.model.MyUserDetails;

public interface UserRepository extends JpaRepository<MyUserDetails, Integer>{

	public MyUserDetails findByEmail(String email);

	public List<MyUserDetails> findByRole(String role);

	public MyUserDetails findByResetToken(String token);
}
