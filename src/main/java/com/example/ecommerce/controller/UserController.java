package com.example.ecommerce.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.model.OrderRequest;
import com.example.ecommerce.model.ProductOrder;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.util.CommonUtil;
import com.example.ecommerce.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m)
	{
		if(p!=null)
		{
			String email = p.getName();
			MyUserDetails myUserDetails = userService.getUserByEmail(email);
			m.addAttribute("user", myUserDetails);
			Integer countCart = cartService.getCountCart(myUserDetails.getId());
			m.addAttribute("countCart",countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys",allActiveCategory);
	}
	
	@GetMapping("/addCart")
	public String addToCard(@RequestParam Integer pid,HttpSession session)
	{
		Cart saveCart = cartService.saveCart(pid, uid);
		
		if(ObjectUtils.isEmpty(saveCart))
		{
			session.setAttribute("errorMsg", "product add to card failed");
		}else
		{
			session.setAttribute("succMsg", "product added to Cart");
		}
		return "redirect:/product/"+pid;
	}
//	@GetMapping("/cart")
//	public String loadCartPage(Principal p, Model m) {
//	    MyUserDetails user = getLoggedInUserDetails(p);
//	    List<Cart> carts = cartService.getcartsByUser(user.getId());
//	    
//	    // Check if the cart is empty before accessing its last element
//	    Double totalOrderPrice = 0.0;
//	    if (!carts.isEmpty()) {
//	        for (Cart cart : carts) {
//	            totalOrderPrice += cart.getTotalPrice();
//	        }
//	    }	
//	    m.addAttribute("carts", carts);
//	    m.addAttribute("totalOrderPrice", totalOrderPrice);
//	    
//	    return "user/cart";
//	}
	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		MyUserDetails user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getcartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		return "/user/cart";
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuality(@RequestParam String action, @RequestParam String sy, @RequestParam Integer cid) {
	    try {
	        cartService.updateCartQuality(action, sy, cid);
	    } catch (Exception e) {
	        // Handle the exception (maybe add an error message to the model)
	        return "redirect:/user/cart?error=" + e.getMessage();
	    }
	    return "redirect:/user/cart";
	}
	
	private MyUserDetails getLoggedInUserDetails(Principal p)
	{
		String email = p.getName();
		MyUserDetails myUserDetails = userService.getUserByEmail(email);
		
		return myUserDetails;
	}
	
	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {
		MyUserDetails user = getLoggedInUserDetails(p);
	    List<Cart> carts = cartService.getcartsByUser(user.getId());
	    m.addAttribute("carts", carts);

	    if (!carts.isEmpty()) {
	        Cart lastCart = carts.get(carts.size() - 1);
	        Double orderPrice = lastCart.getTotalOrderPrice();
	        Double totalOrderPrice = orderPrice + 250 + 100;  // Add the extra charges
	        m.addAttribute("orderPrice", orderPrice);
	        m.addAttribute("totalOrderPrice", totalOrderPrice);
	    }

	    return "/user/order";
	}
	
	@PostMapping("/save-orders")
	public String saveOrder(@ModelAttribute OrderRequest request,Principal p) throws Exception
	{
//		System.out.println(request);
		MyUserDetails user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadsucess()
	{
		return "user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m,Principal p)
	{
		MyUserDetails loginUser= getLoggedInUserDetails(p);
		List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
		m.addAttribute("orders",orders);
		return "user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}
		
		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/user-orders";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute MyUserDetails user, @RequestParam MultipartFile img, HttpSession session) {
		MyUserDetails updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session)
	{
		MyUserDetails loggedInUserDetails = getLoggedInUserDetails(p);
		
		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
		
		if(matches)
		{
			String encodePassword = passwordEncoder.encode(newPassword);
			loggedInUserDetails.setPassword(encodePassword);
			MyUserDetails updateUser = userService.updateUser(loggedInUserDetails);
			
			if(ObjectUtils.isEmpty(updateUser)) {
				session.setAttribute("succMsg", "Password not updated || Something Wrong on Server");
			}else {
				session.setAttribute("succMsg", "Profile Updated Succesfully");
			}
		}else
		{
			session.setAttribute("errorMsg", "Current Password is Incorrect");
		}
		
		return "redirect:/user/profile";
	}
}
