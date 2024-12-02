package com.example.ecommerce.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ecommerce.model.Cart;

public interface CartService {

	public Cart saveCart(Integer product,Integer userId);
	
	public List<Cart> getcartsByUser(Integer userId);
	
	public Integer getCountCart(Integer userId);

	public void updateCartQuality(String action,String sy, Integer cid) throws Exception;

	
}
