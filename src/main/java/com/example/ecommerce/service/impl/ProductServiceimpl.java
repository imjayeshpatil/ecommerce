package com.example.ecommerce.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductReport;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;

@Service
public class ProductServiceimpl implements ProductService{

	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public Product saveProduct(Product product) {
		product.setStockAddedDate(new Date().toString());
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		Product product = productRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(product)) {
			productRepository.delete(product);
			return true;
		}
		return false;
	}

	@Override
	public Product getProductById(Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product,MultipartFile image) {
	Product dbProduct = getProductById(product.getId());
	if(dbProduct.getStock()!=product.getStock()) {
		dbProduct.setStockAddedDate(new Date().toString());		
	}
	String imageName =image.isEmpty() ? dbProduct.getImage() : image.getOriginalFilename();
	
	dbProduct.setTitle(product.getTitle());
	dbProduct.setDescription(product.getTitle());
	dbProduct.setCategory(product.getCategory());
	dbProduct.setPrice(product.getPrice());
	dbProduct.setStock(product.getStock());
	dbProduct.setImage(imageName);
	dbProduct.setIsActive(product.getIsActive());
	
	dbProduct.setDiscount(product.getDiscount());
	
	//s=100(5/100); 100-5=95
	Double discount= product.getPrice()*(product.getDiscount()/100.0);
	
	Double discountPrice=product.getPrice()-discount;
	
	dbProduct.setDiscountPrice(discountPrice);
	
	Product updateProduct = productRepository.save(dbProduct);
	
	
	if (!ObjectUtils.isEmpty(updateProduct)) {

		if (!image.isEmpty()) {

			try {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
						+ image.getOriginalFilename());
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return product;
	}

	return null;
}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products = null;
		if(ObjectUtils.isEmpty(category))
		{
			products=productRepository.findByIsActiveTrue();
		}else
		{
			products=productRepository.findByCategory(category);
		}
		
		return products;
	}

	@Override
	public List<Product> getLowStockProducts() {
	    return productRepository.findByStockLessThan(15); // This assumes you have a method in your repository to fetch products with low stock
	}
	
	

	 @Override
	 public List<ProductReport> getProductReport(String fromDate, String toDate, Long productId) {
		    List<ProductReport> reportList = new ArrayList<>();
		    
		    // Fetch the product by its ID
		    Product product = productRepository.findById(productId)
		            .orElseThrow(() -> new RuntimeException("Product not found"));

		    // Ensure that fromDate and toDate are properly parsed to LocalDate
		    // Here we use DateTimeFormatter to handle the possible variations in date formats if needed.
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		    LocalDate from = LocalDate.parse(fromDate, formatter);
		    LocalDate to = LocalDate.parse(toDate, formatter);

		    // Get the sold stock for the product (Make sure this method exists in the repository)
		    Integer soldStock = productRepository.findSoldStock(productId, "Delivered", from, to)
		            .orElse(0); // Default to 0 if no sold stock is found

		    // Calculate the available stock
		    int availableStock = product.getStock() - soldStock;

		    // Create a report object and add it to the list
		    ProductReport report = new ProductReport();
		    report.setProductId(productId);
		    report.setTitle(product.getTitle());
		    report.setSoldStock(soldStock);
		    report.setAvailableStock(availableStock);

		    reportList.add(report);

		    return reportList;
		}

	

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}
	}


	
	
	
	

	

	

	

	
	


	
	
	

