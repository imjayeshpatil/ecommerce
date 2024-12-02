package com.example.ecommerce.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductReport;



public interface ProductService {
	
	public Product saveProduct(Product product);
	
	public List<Product> getAllProducts();
	
	public Boolean deleteProduct(Integer id);
	
	public Product getProductById(Integer id);
	
	public Product updateProduct(Product product,MultipartFile file);
	
	public List<Product> getLowStockProducts(); // This assumes you have a method in your repository to fetch products with low stock

//	public List<ProductReport> getProductReport(String fromDate, String toDate, Long productId);
	List<ProductReport> getProductReport(String fromDate, String toDate, Long productId);
	
	public List<Product> getAllActiveProducts(String category);

	public List<Product> searchProduct(String ch);

	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category);

	
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch);

	Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize);

	
	
	
    
	}

