package com.z.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.z.ErrorBean;
import com.z.beans.Book;
import com.z.beans.Books;
import com.z.config.DataSourceConfig;
import com.z.utils.DBCleanup;

@RestController
public class BookLoaderController {

	@Autowired
	DataSourceConfig mainDataSource;
	
	@RequestMapping(method = RequestMethod.POST, value="/loadbooks")
	@ResponseBody
	public ErrorBean checkUsername(@RequestBody ErrorBean props) {
		ErrorBean bean = new ErrorBean();
		ObjectMapper mapper = new ObjectMapper();
		File f = new File(props.getMessage());
		try {
			Books books = mapper.readValue(f, Books.class);
			System.out.println("Number of books: " + books.getBooks().length);
			insertBooks(books, bean);
//			bean.setMessage("Number of books: " + books.getBooks().length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bean.setMessage(e.getMessage());
		}
		
		return bean;
	}
	
	private void insertBooks(Books bks, ErrorBean bean) {
		Connection conn = null;
		PreparedStatement stmt = null;
		int cnt = 0;
		try {
			conn = this.mainDataSource.createMainDataSource().getConnection();
			stmt = conn.prepareStatement(
			    "insert into book (ISBN, TITLE, AUTHOR, DESCRIPTION, YEAR, PAGE, PRICE, QUANTITY) " +
			    "VALUES( ?,?,?,?,?,?,?,?)");
			for (Book book : bks.getBooks()) {
				System.out.println(cnt + " : " + book.getIsbn());
				stmt.setString(1, book.getIsbn());
				stmt.setString(2, book.getTitle());
				stmt.setString(3, book.getAuthor());
				stmt.setString(4, book.getDescription());
				stmt.setInt(5, book.getYear());
				stmt.setInt(6, book.getPages());
				stmt.setDouble(7, book.getPrice());
				stmt.setInt(8, book.getQuantity());
				cnt += stmt.executeUpdate();
			}
			conn.commit();
			bean.setStatus("OK");
			bean.setMessage(cnt + " out of " + bks.getBooks().length + " books added.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bean.setMessage(e.getMessage());
			bean.setStatus("Error");
		}
		finally {
			DBCleanup.cleanup(conn, stmt, null);
		}
	}
}
