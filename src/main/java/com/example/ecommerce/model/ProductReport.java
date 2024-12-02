package com.example.ecommerce.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ProductReport {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String title;
    
    private int soldStock;
    @Override
	public String toString() {
		return "ProductReport [id=" + productId + ", title=" + title + ", soldStock=" + soldStock + ", availableStock="
				+ availableStock + "]";
	}

	private int availableStock;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSoldStock() {
        return soldStock;
    }

    public void setSoldStock(int soldStock) {
        this.soldStock = soldStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	
}
