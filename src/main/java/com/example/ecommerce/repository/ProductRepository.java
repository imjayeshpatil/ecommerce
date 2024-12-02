package com.example.ecommerce.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Optional<Product> findById(Long id);

	@Modifying
	@Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId")
	void updateStock(@Param("productId") Long productId, @Param("quantity") int quantity);

	// Fetching the total sold stock for a product within a date range and status
	@Query("SELECT COALESCE(SUM(po.quantity), 0) FROM ProductOrder po "
			+ "WHERE po.product.id = :productId AND po.status = :status "
			+ "AND po.orderDate BETWEEN :fromDate AND :toDate")
	Optional<Integer> findSoldStock(@Param("productId") Long productId, @Param("status") String status,
			@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

	List<Product> findByIsActiveTrue();  //done

	List<Product> findByCategory(String category);  //done

	List<Product> findByStockLessThan(int stock);

	List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2); //done
	
	Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

	Page<Product> findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2,
			Pageable pageable);

	Page<Product> findByIsActiveTrue(Pageable pageable); //done

	Page<Product> findByCategory(Pageable pageable, String category);		//done
	
	
	
	
}
