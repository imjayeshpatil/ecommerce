package com.example.ecommerce.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

//import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private CommonUtil commonUtil;
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
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
	
	@GetMapping("/")
	public String index() {
		return "Index";
	}
	
	@GetMapping("/signin")
	public String login() {
		return "login";
	}
	
	@GetMapping("/register")
	public String register() {
		return "register";
	}
	
	
	@GetMapping("/products")
	public String products(Model m,String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);
		Page<Product> page = null;
		if (StringUtils.isEmpty(ch)) {
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
		}

		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "product";
	}
	
	
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute MyUserDetails user,HttpSession session) throws IOException
	{
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		MyUserDetails saveUser = userService.saveUser(user);
		
		if(!ObjectUtils.isEmpty(saveUser))
		{
			if(!file.isEmpty())
			{
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Register successfully");
		}else {
			
			
			session.setAttribute("errorMsg", "Something Wrong on Server ");
		}
		return "redirect:/register";
	}
	
	//forget Password code
	
	@GetMapping("/forget-password")
	public String showForgotPassword()
	{
		return "forgetpassword.html";
	}
	
	@PostMapping("/forget-password")
	public String processForgotPassword(@RequestParam String email,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException
	{
		
		MyUserDetails userByEmail = userService.getUserByEmail(email);
		
		
		if(ObjectUtils.isEmpty(userByEmail))
		{
			session.setAttribute("errorMsg", "invalid Email");
		}else
		{
			
			String resetToken = UUID.randomUUID().toString();
			userService.updateUserRestToken(email,resetToken);
			
			//Genrate URL: http://localhost:8080/reset-password?token=goodmorning
			
			String url = CommonUtil.generateUrl(request)+"/reset_password?token="+resetToken;
			
			Boolean sendMail = commonUtil.sendMail(url,email);
			
			if(sendMail)
			{
				session.setAttribute("succMsg", "Please Check your mail..Password is Rest link set");
			}else
			{
				session.setAttribute("errorMsg", "Something Wrong on Server: | Mail Not Send ");
			}
		}
		return "redirect:/forget-password";
	}
	
	
	@GetMapping("/reset_password")
	public String showResetPassword(@RequestParam String token, HttpSession session,Model m)
	{
		MyUserDetails userByToken = userService.getUserByToken(token);
//		System.out.println(userByToken);
		if(userByToken==null) {
			m.addAttribute("errorMsg", "Your link is Invalid or Expired");
			return "error";
		}
		
		m.addAttribute("token", token);
		return "reset_password";
	}
	
	
	@GetMapping("/category")
	public String getActiveCategory(Model m)
	{
		
		return "null";
	}
	
	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Model m) {
		List<Product> searchProducts = productService.searchProduct(ch);
		m.addAttribute("products", searchProducts);
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
		return "product";

	}
}
