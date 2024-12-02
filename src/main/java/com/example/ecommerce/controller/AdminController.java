package com.example.ecommerce.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.grammars.hql.HqlParser.IsEmptyPredicateContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.MyUserDetails;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductOrder;
import com.example.ecommerce.model.ProductReport;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;

import com.example.ecommerce.service.UserService;
import com.example.ecommerce.service.impl.ProductServiceimpl;
import com.example.ecommerce.util.CommonUtil;
import com.example.ecommerce.util.OrderStatus;

import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;
	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			MyUserDetails myUserDetails = userService.getUserByEmail(email);
			m.addAttribute("user", myUserDetails);
			Integer countCart = cartService.getCountCart(myUserDetails.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {

		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {

		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);

		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m,@RequestParam (name= "pageNo",defaultValue = "0") Integer pageNo,@RequestParam (name= "pageSize", defaultValue = "4") Integer pageSize ) {

//		m.addAttribute("categorys", categoryService.getAllCategory());

		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);
		

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean exitCategory = categoryService.exitCategory(category.getName());

		if (exitCategory) {
			session.setAttribute("errorMsg", "Category Name already exist");
		} else {
			Category saveCategory = categoryService.saveCategory(category);
			if (ObjectUtils.isEmpty(saveCategory)) {

				session.setAttribute("errorMsg", "not saved internal server error");

			} else {

				File saveFile = new File("src/main/resources/static/img");

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("succMsg", "Saved successfully");
			}
		}

		categoryService.saveCategory(category);

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category Delete success");
		} else {
			session.setAttribute("errorMsg", "Something Wrong on Server");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		Category oldcategory = categoryService.getCategoryById(category.getId());

		String imageName = file.isEmpty() ? oldcategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {
			oldcategory.setName(category.getName());
			oldcategory.setIsActive(category.getIsActive());
			oldcategory.setImageName(imageName);

		}

		Category updateCategory = categoryService.saveCategory(oldcategory);
		System.out.println(updateCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			session.setAttribute("succMsg", "category update successfully");
		} else {
			session.setAttribute("errorMsg", "Some thing Wrong on Server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();

	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {
		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImage(imageName);
		product.setStockAddedDate(new Date().toString());
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ image.getOriginalFilename());

			// System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("succMsg", "Product Save Successfully");
		} else {
			session.setAttribute("errorMsg", "Something Wrong on Server ");
		}

		return "redirect:/admin/loadAddProduct";
	}
	
	
	@GetMapping("/view_products")
	public String loadViewProduct(Model m) String ch) {
		List<Product> Products=null;
		if(ch!=null && ch.length()>0)
		{
			Products = productService.searchProduct(ch);
		}else
		{
			Products=productService.getAllProducts();
		}

		m.addAttribute("view_products",Products);
	    return "admin/view_products";
	}
	

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete successfully");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/view_products";
	}

	@GetMapping("/edit_product/{id}")
	public String edit_product(@PathVariable int id, Model m) {

		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "Invalid Discount");
		} else {

			Product updateProduct = productService.updateProduct(product, image);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("succMsg", "Product update success");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}
		}
		return "redirect:/admin/edit_product/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m,@RequestParam Integer type) {
		List<MyUserDetails> users = null;
		if(type==1) 
		{
			users = userService.getUsers("ROLE_USER");
		}else
		{
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		m.addAttribute("users", users);
		return "admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Account status Updated");
		} else {
			session.setAttribute("errorMsg", "Something wrong on Server");

		}
		return "redirect:/admin/users";
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam (name= "pageNo",defaultValue = "0") Integer pageNo,
			@RequestParam (name= "pageSize", defaultValue = "4") Integer pageSize ) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch",false);
		
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		
		return "admin/orders";
	}

	@PostMapping("/update-order-status")
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
		return "redirect:/admin/orders";
	}

	@GetMapping("/view_notification") // URL to trigger the low stock product view
	public String getLowStockProductsPage(Model model) {
		// Fetch products with low stock (stock < 15)
		List<Product> lowStockProducts = productService.getLowStockProducts();

		// Add the low-stock products to the model to be displayed in the view
		model.addAttribute("view_products", lowStockProducts);

		// Return the view (the view that will display the low-stock products)
		return "admin/view_notification"; // Return the Thymeleaf template for notification
	}

	@GetMapping("view_reports")
	public String viewReport(Model model) {

		List<Product> products = productService.getAllProducts(); // Fetch products
	    model.addAttribute("products", products); // Add to model
		// Get product report
//		List<ProductReport> reports = productService.getProductReport(fromDate, toDate, productId);
//
//		// Add the reports and product list to the model
//		model.addAttribute("reports", reports);
//		model.addAttribute("products", productService.getAllProducts());

		
		return "admin/view_reports"; // Return the view_report HTML page
	}
	
	@Autowired
	private ProductServiceimpl serviceimpl;
	@GetMapping("GetReport")
	public ResponseEntity<?> GetReport(
	        @RequestParam(value = "fromDate", required = false, defaultValue = "2024-01-01") String fromDate,
	        @RequestParam(value = "toDate", required = false, defaultValue = "2024-12-31") String toDate,
	        @RequestParam("productId") Long productId) {

	    try {
	        // Fetch the product report
	        List<ProductReport> reports = serviceimpl.getProductReport(fromDate, toDate, productId);

	        // Fetch all products for the dropdown
	        List<Product> products = productService.getAllProducts();

	        // Create a response object to return
	        Map<String, Object> response = new HashMap<>();
	        response.put("reports", reports);
	        response.put("products", products);
	        if(reports!=null)
	        {
	        	response.put("message","Data Found");
	        }
	        else {
	        	response.put("message","No Data Found");
	        }
	        response.put("products", products);

	        // Return the data as JSON
	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        e.printStackTrace(); // Log error details
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "An error occurred while fetching the report."));
	    }
	}

	
	
	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, 
			Model m,HttpSession session,@RequestParam (name= "pageNo",defaultValue = "0") Integer pageNo,
			@RequestParam (name= "pageSize", defaultValue = "4") Integer pageSize ) {
		
		if(orderId!=null && orderId.length()>0)
		{	
		ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
		
		if(ObjectUtils.isEmpty(order))
		{
			session.setAttribute("errorMsg","Incorrect orderId");
			m.addAttribute("orderDtls", null);
		}else
		{
			m.addAttribute("orderDtls",order);
		}
		m.addAttribute("srch",true);
		}else {
			/*
			 * List<ProductOrder> allOrders = orderService.getAllOrders();
			 * m.addAttribute("orders", allOrders); m.addAttribute("srch",false);
			 */
			
			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch",false);
			
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
			
			
		}
		return "/admin/orders";

	}
	
	@GetMapping("/add_admin")
	public String loadAdminAdd()
	{
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute MyUserDetails user,@RequestParam ("img") MultipartFile file,HttpSession session) throws IOException
	{
		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		MyUserDetails saveUser = userService.saveAdmin(user);
		
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
		return "admin/add_admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "admin/profile";
	}
	
	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute MyUserDetails user, @RequestParam MultipartFile img, HttpSession session) {
		MyUserDetails updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		} else {
			session.setAttribute("succMsg", "Profile Updated");
		}
		return "redirect:/admin/profile";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session)
	{
		MyUserDetails loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
		
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
		
		return "redirect:/admin/profile";
	}
	
	
	
	
	

	
	/*
	 * @GetMapping("/search") public String searchProduct(@RequestParam String ch,
	 * Model m) {
	 * 
	 * m.addAttribute("products", searchProducts);
	 * 
	 * return "view_products";
	 * 
	 * }
	 */
	

	
	
}
