package com.example.ecommerce.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.OrderAddress;
import com.example.ecommerce.model.OrderRequest;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductOrder;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductOrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.util.CommonUtil;
import com.example.ecommerce.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService{

	@Autowired
	private ProductOrderRepository orderRepository;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws Exception {
	    List<Cart> carts = cartRepository.findByUserId(userId);
	    
	    for (Cart cart : carts) {
	        ProductOrder order = new ProductOrder();
	        
	        // Set the order details
	        order.setOrderId(UUID.randomUUID().toString());
	        order.setOrderDate(LocalDate.now());
	        order.setProduct(cart.getProduct());
	        order.setPrice(cart.getProduct().getDiscountPrice());
	        order.setQuantity(cart.getQuantity());
	        order.setUser(cart.getUser());
	        order.setStatus(OrderStatus.IN_PROGRESS.name());
	        order.setPaymentType(orderRequest.getPaymentType());

	        // Set the shipping address
	        OrderAddress address = new OrderAddress();
	        address.setFirstName(orderRequest.getFirstName());
	        address.setLastName(orderRequest.getLastName());
	        address.setEmail(orderRequest.getEmail());
	        address.setMobileNo(orderRequest.getMobileNo());
	        address.setAddress(orderRequest.getAddress());
	        address.setCity(orderRequest.getCity());
	        address.setState(orderRequest.getState());
	        address.setPincode(orderRequest.getPincode());
	        
	        order.setOrderAddress(address);

	        // Save the order to the database
	        ProductOrder savedOrder = orderRepository.save(order);

	        // Update product stock after placing the order
	        Product product = cart.getProduct();
	        int remainingStock = product.getStock() - cart.getQuantity();
	        if (remainingStock >= 0) {
	            product.setStock(remainingStock);
	            // Save the updated product stock
	            productRepository.save(product);
	        } else {
	            throw new Exception("Insufficient stock for product: " + product.getName());
	        }

	        // Send an email notification for the order
	        commonUtil.sendMailForProductOrder(order, "success");
	    }
		
		
	}

	@Override
	public List<ProductOrder> getOrdersByUser(Integer userId) {
		List<ProductOrder> orders=orderRepository.findByUserId(userId);
		return orders;
	}
	
	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = orderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		
		return	orderRepository.findAll();
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {
		return orderRepository.findByOrderId(orderId);
	}

	@Override
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);

	}
	
	
	

}
