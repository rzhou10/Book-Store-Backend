package com.z.beans;

public class Book {
	private String isbn;
	private String title;
	private String author;
	private String description;
	private Integer year;
	private Integer pages;
	private Double price;
	private Integer quantity;
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || ! (other instanceof Book)) {
			return false;
		}
		Book o = (Book) other;
		if (other == this) {
			return true;
		}
		else {
			return this.getIsbn().equals(o.getIsbn());
		}		
	}
	
	public int hashCode(){
		return isbn == null ? 0 : Integer.valueOf(isbn);
	}

}
