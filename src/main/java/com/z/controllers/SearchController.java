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

import com.z.beans.*;
import com.z.config.DataSourceConfig;
import com.z.utils.DBCleanup;

@RestController
public class SearchController implements ControllerHelper {
	@Autowired
	DataSourceConfig mainDataSource;
	
// curl -X GET -i 'http://localhost:8081/search?searchTerm=boot'	
	@RequestMapping(method = RequestMethod.GET, value="/search")
	@CrossOrigin(origins = "http://localhost:3000")
	public List<Book> searchBooks(@RequestParam(value="searchTerm", defaultValue="") String name) {
		List<Book> books = new ArrayList<Book>();
		if (! "".equals(name)) {
			String[] terms = name.split("\\s+");
			StringBuilder sb = new StringBuilder();
			String and = "";
			for (String s : terms) {
				String or = "";
				sb.append(and);
				sb.append(or + " ( upper(author) like upper('%" + s + "%')");
				or = " or";
				sb.append(or + " upper(title) like upper('%" + s + "%')");
				sb.append(or + " upper(description) like upper('%" + s + "%')");
				sb.append(or + " upper(isbn) like upper('%" + s + "%')");
				sb.append(or + " upper(year) like upper('%" + s + "%') ) ");
				and = " and ";
			}
			System.out.println( sb.toString() );
			if ( ! search(books, sb.toString()) ) {
				throw new ResponseStatusException(
				          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request");
			}
		}
		return books;
	}
	
	private boolean search(List<Book> books, String where) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.mainDataSource.createMainDataSource().getConnection();
			stmt = conn.prepareStatement(
					"select * from book where " + where);
			rs = stmt.executeQuery();
			while (rs.next()) {
				books.add(toBook(rs));
			}
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally {
			DBCleanup.cleanup(conn, stmt, rs);
		}
	}
}
