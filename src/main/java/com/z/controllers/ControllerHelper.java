package com.z.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.z.beans.Account;
import com.z.beans.Book;
import com.z.beans.Orders;

public interface ControllerHelper {
	
	public default Book toBook(ResultSet rs) throws SQLException {
		Book b = new Book();
		b.setIsbn(rs.getString("ISBN"));
		b.setAuthor(rs.getString("AUTHOR"));
		b.setTitle(rs.getString("TITLE"));
		b.setDescription(rs.getString("DESCRIPTION"));
		b.setYear(rs.getInt("YEAR"));
		b.setPages(rs.getInt("PAGE"));
		b.setQuantity(rs.getInt("QUANTITY"));
		b.setPrice(Math.round(rs.getDouble("PRICE")*100.00)/100.00);
		return b;
	}
	
	public default Orders toOrders(ResultSet rs) throws SQLException {
		Orders orders = new Orders();
		orders.setAccountNumber(rs.getInt("ACCOUNT_NUMBER"));
		orders.setOrderNumber(rs.getInt("ORDER_NUMBER"));
		orders.setOrderTime(rs.getTimestamp("ORDER_TIME"));
		orders.setStatus(rs.getString("STATUS"));
		orders.setTotal(Math.round(rs.getDouble("TOTAL_PRICE")*100.00)/100.00);
		return orders;
	}
	
	public default Account toAccount(ResultSet rs) throws SQLException {
		Account a = new Account();
		a.setAccountNumber(rs.getInt("ACCOUNT_NUMBER"));
		a.setCity(rs.getString("CITY"));
		a.setEmail(rs.getString("EMAIL"));
		a.setFirstName(rs.getString("FIRST_NAME"));
		a.setLastName(rs.getString("LAST_NAME"));
		a.setPhone(rs.getString("PHONE"));
		a.setState(rs.getString("STATE"));
		a.setStreet(rs.getString("STREET"));
		a.setUsername(rs.getString("USERNAME"));
		a.setZip(rs.getString("ZIP"));
		a.setPassword("*****");
		return a;
	}

}
