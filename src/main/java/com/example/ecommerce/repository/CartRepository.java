package com.example.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer>{

	public Cart findByUserIdAndProductId(Integer productId, Integer userId);

	public List<Cart> findByUserId(Integer userId);

	public Integer countByUserId(Integer userId);
}
