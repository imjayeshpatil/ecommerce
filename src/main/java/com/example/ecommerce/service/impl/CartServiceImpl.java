package com.example.ecommerce.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {

        MyUserDetails myUserDetails = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        Cart cartStatus = cartRepository.findByUserIdAndProductId(userId, productId);

        Cart cart;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(myUserDetails);
            cart.setQuantity(1);
            cart.setTotalPrice(roundPrice(1 * product.getDiscountPrice()));
        } else {
            cart = cartStatus;
            Integer currentQuantity = cart.getQuantity() != null ? cart.getQuantity() : 0; // Fallback to 0 if null
            cart.setQuantity(currentQuantity + 1);
            cart.setTotalPrice(roundPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice()));
        }
        return cartRepository.save(cart);
    }

    @Override
    public List<Cart> getcartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        Double totalOrderPrice = 0.0;
        List<Cart> updateCarts = new ArrayList<>();

        for (Cart c : carts) {
            Double discountPrice = c.getProduct().getDiscountPrice();
            Integer quantity = c.getQuantity() != null ? c.getQuantity() : 0; 
            Double totalPrice = roundPrice(discountPrice * quantity);
            c.setTotalPrice(totalPrice);

            totalOrderPrice += totalPrice;
            c.setTotalOrderPrice(roundPrice(totalOrderPrice));
            updateCarts.add(c);
        }

        return updateCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {
        return cartRepository.countByUserId(userId);
    }

    
    private Double roundPrice(Double price) {
        BigDecimal bigDecimalPrice = new BigDecimal(price);
        return bigDecimalPrice.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    

	@Override
	public void updateCartQuality(String action, String sy, Integer cid) throws Exception {
		Optional<Cart> cartOpt = cartRepository.findById(cid);

        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            Product product = cart.getProduct();
            
            int availableStock = product.getStock();

            if (action.equals("in")) { 
                if (cart.getQuantity() < availableStock) {
                    cart.setQuantity(cart.getQuantity() + 1);
                    cartRepository.save(cart); 
                } else {
                    throw new Exception("Product stock is not sufficient for your requested quantity.");
                }
            } else if (action.equals("de")) { 
                if (cart.getQuantity() > 1) { 
                    cart.setQuantity(cart.getQuantity() - 1);
                    cartRepository.save(cart); 
                } else if (cart.getQuantity() == 1) {
                    cartRepository.delete(cart); 
                } else {
                    throw new Exception("Quantity can't be less than 1.");
                }
            }
        } else {
            throw new Exception("Cart item not found.");
        }
		
	}

		
	}


    
