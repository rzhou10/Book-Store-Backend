package com.z.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
public class OrderController implements ControllerHelper {
	@Autowired
	DataSourceConfig mainDataSource;
	
// curl -X GET -i 'http://localhost:8081/listOrders?acctNumber=3'	
	@RequestMapping(method = RequestMethod.GET, value="/listOrders")
	@CrossOrigin(origins = "http://localhost:3000")
	public List<Orders> listOrders(@RequestParam(value="acctNumber", defaultValue="0") Integer acctNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Orders> orders = new ArrayList<>();
        try {
        	conn = mainDataSource.createMainDataSource().getConnection();
        	//get all orders for the given account number
			stmt = conn.prepareStatement("select * from orders where account_number = ? and status != 'Cart' order by order_time desc");
			stmt.setInt(1, acctNumber);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Orders order = toOrders(rs);
				orders.add(order);
				// for each order get the list of books
				PreparedStatement stmt1 = conn.prepareStatement("select b.* " + 
						"from    book b, order_detail d " + 
						"where d.order_number = ? " + 
						"and    d.isbn = b.isbn;");
				stmt1.setInt(1, order.getOrderNumber());
				ResultSet rs1 = stmt1.executeQuery();
				List<Book> books = new ArrayList<>();
				while (rs.next()) {
					books.add(toBook(rs));
				}
				order.setBooks(books);
				rs1.close();
				stmt1.close();
			}
			if (orders.isEmpty()) {
				throw new ResponseStatusException(
				          HttpStatus.NOT_FOUND, "No prior order for account " + acctNumber);
			}
        }catch (SQLException e) {
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
        return orders;
	}
	
// http://localhost:8081/placeOrders?orderNumber=1	
	@RequestMapping(method = RequestMethod.GET, value="/placeOrders")
	@CrossOrigin(origins = "http://localhost:3000")
	public int placeOrders(@RequestParam(value="orderNumber", defaultValue="") Integer orderNumber) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        double total = 0.0;
        try {
			conn = mainDataSource.createMainDataSource().getConnection();
			// gets the total price for all books in the cart
			stmt = conn.prepareStatement("select sum(price)  " + 
					"from    book b, order_detail d " + 
					"where d.order_number = ? " + 
					"and    b.isbn = d.isbn");
			stmt.setInt(1, orderNumber);
			rs = stmt.executeQuery();
			if (rs.next()) {
				total = rs.getDouble(1);
			}
			else {
				throw new ResponseStatusException(
				          HttpStatus.NO_CONTENT, "Error processing request");
			}
			rs.close();
			stmt.close();
			
			// update the orders entry in Cart status to Pending plus update total price
			stmt = conn.prepareStatement("update orders set status = 'Pending', total_price = ?, order_time = current_timestamp where order_number = ?");
			stmt.setInt(2, orderNumber);
			stmt.setDouble(1, total);
			int cnt = stmt.executeUpdate();
			conn.commit();
			return cnt;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
	}
}
