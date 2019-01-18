package com.z.controllers;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.z.beans.Book;
import com.z.beans.Orders;
import com.z.config.DataSourceConfig;
import com.z.utils.DBCleanup;

@RestController
public class CartController implements ControllerHelper {
	@Autowired
	DataSourceConfig mainDataSource;
	
// curl -X GET -i 'http://localhost:8081/cartCount?acctNumber=3'	
	@RequestMapping(method = RequestMethod.GET, value="/cartCount")
	@CrossOrigin(origins = "http://localhost:3000")
	public int getItemCountInCart(@RequestParam(value="acctNumber", defaultValue="") Integer acctNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
			conn = mainDataSource.createMainDataSource().getConnection();
			stmt = conn.prepareStatement("select count(*) from orders o, order_detail d where o.account_number = ? and o.order_number = d.order_number and o.status = 'Cart'");
			stmt.setInt(1, acctNumber);
			rs = stmt.executeQuery();
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
	}
	
// curl -X GET -i 'http://localhost:8081/addToCart?acctNumber=3&isbn=9781986393898'	
	@RequestMapping(method = RequestMethod.GET, value="/addToCart")
	@CrossOrigin(origins = "http://localhost:3000")
	public int addToCart(@RequestParam(value="acctNumber", defaultValue="") Integer acctNumber,
			@RequestParam(value="isbn", defaultValue="") String isbn) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
        	Integer bookNumber = null;
        	conn = mainDataSource.createMainDataSource().getConnection();
        	// try to find existing Cart entry in table ORDERS
			stmt = conn.prepareStatement("select order_number from orders where account_number = ? and status = 'Cart'");
			stmt.setInt(1, acctNumber);
			rs = stmt.executeQuery();
			if (rs.next()) {
			    bookNumber = rs.getInt(1);
			    rs.close();
			}
			else {
				// if no Cart yet, create one
				PreparedStatement stmt1 = conn.prepareStatement("insert into ORDERS (ACCOUNT_NUMBER, ORDER_TIME,TOTAL_PRICE, STATUS) " 
						+ "values(?,current_timestamp,?,?)");
				stmt1.setInt(1, acctNumber);
				stmt1.setDouble(2, 0);
				stmt1.setString(3, "Cart");
				stmt1.executeUpdate();
				
				rs = stmt.executeQuery();
				if (rs.next()) {
				    bookNumber = rs.getInt(1);
				}
				rs.close();
				stmt1.close();
			}
			// add an entry in ORDER_DETAIL for the Cart
			System.out.println(isbn);
			PreparedStatement stmt2 = conn.prepareStatement("insert into order_detail(order_number, isbn, quantity) " 
					+ " values (?,?,?)");
			stmt2.setInt(1, bookNumber);
			stmt2.setString(2, isbn);
			stmt2.setInt(3, 1);
			int cnt = stmt2.executeUpdate();
			conn.commit();
			stmt2.close();
			
			return cnt;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
	}
	
//  curl -X GET -i 'http://localhost:8081/removeFromCart?acctNumber=3&isbn=9781986393898'	
	@RequestMapping(method = RequestMethod.GET, value="/removeFromCart")
	@CrossOrigin(origins = "http://localhost:3000")
	public int removeFromCart(@RequestParam(value="acctNumber", defaultValue="") Integer acctNumber,
			@RequestParam(value="isbn", defaultValue="") String isbn) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
        	Integer orderNumber = null;
        	conn = mainDataSource.createMainDataSource().getConnection();
        	// Find Cart first
			stmt = conn.prepareStatement("select order_number from orders where account_number = ? and status = 'Cart';");
			stmt.setInt(1, acctNumber);
			rs = stmt.executeQuery();
			if (rs.next()) {
				orderNumber = rs.getInt(1);
				// then delete the given book from Cart
			    PreparedStatement stmt1 = conn.prepareStatement("delete from order_detail where order_number = ? and isbn = ?");
			    stmt1.setInt(1, orderNumber);
			    stmt1.setString(2, isbn);
			    stmt1.executeUpdate();
			    conn.commit();
			    stmt1.close();
			}
			else {				
				return 0;
			}
			return 1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
	}
	
// curl -X GET -i 'http://localhost:8081/listItemsInCart?acctNumber=3'	
	@RequestMapping(method = RequestMethod.GET, value="/listItemsInCart")
	@CrossOrigin(origins = "http://localhost:3000")
	public Orders listItemsInCart(@RequestParam(value="acctNumber", defaultValue="") Integer acctNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        Orders cart = new Orders();
        try {
        	conn = mainDataSource.createMainDataSource().getConnection();
        	// find Cart first
			stmt = conn.prepareStatement("select * from orders where account_number = ? and status = 'Cart'");
			stmt.setInt(1, acctNumber);
			rs = stmt.executeQuery();
			if (rs.next()) { // should have one only
				cart = toOrders(rs);
				rs.close();
				stmt.close();
				// gets all books for the Cart
				PreparedStatement stmt1 = conn.prepareStatement("select b.* " + 
						"from    book b, order_detail d " + 
						"where d.order_number = ? " + 
						"and    d.isbn = b.isbn;");
				stmt1.setInt(1, cart.getOrderNumber());
				ResultSet rs1 = stmt1.executeQuery();
				List<Book> books = new ArrayList<>();
				while (rs1.next()) {
					books.add(toBook(rs1));
				}
				cart.setBooks(books);
				cart.setTotal(this.getTotal(books));
				rs1.close();
				stmt1.close();
			}
			else {
				throw new ResponseStatusException(
				          HttpStatus.NOT_FOUND, "Cart is empty");
			}
        }catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
        return cart;
	}
	
	private double getTotal(List<Book> books) {
		double total = 0.0;
		for (Book b : books) {
			total += b.getPrice();
		}
		return total;
	}

}
