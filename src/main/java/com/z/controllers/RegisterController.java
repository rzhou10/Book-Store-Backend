package com.z.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.z.ErrorBean;
import com.z.beans.Account;
import com.z.config.DataSourceConfig;
import com.z.utils.DBCleanup;

@RestController
public class RegisterController implements ControllerHelper {
	@Autowired
	DataSourceConfig mainDataSource;
	
// curl -X POST -H 'Content-Type: application/json' -i http://localhost:8081/checkUsername --data '{"firstName":"John","lastName":"Doe","street":"23 Main Street","city":"New York","state":"NY","zip":"10002","phone":"123-456-7890","email":"jdoe@mail.com","username":"jdoe","password":"asfsafd"}'	
	@RequestMapping(method = RequestMethod.POST, value="/checkUsername")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	public ErrorBean checkUsername(@RequestBody Account account) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        ErrorBean b = new ErrorBean();
		try {
			conn = mainDataSource.createMainDataSource().getConnection();
			Account a = findAccount(conn, account.getUsername());
			if (a.getAccountNumber() != null) {
				b.setStatus("ERROR");
				b.setMessage("Username " + account.getUsername() + " has been taken.");
			}
			else {
				b.setStatus("OK");
				b.setMessage("OK");
			}
			return b;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ResponseStatusException(
			          HttpStatus.SERVICE_UNAVAILABLE, "Error processing request", e);
		}
        finally {
        	DBCleanup.cleanup(conn, stmt, rs);
        }
	}
	
	
// curl -X POST -H 'Content-Type: application/json' -i http://localhost:8081/register --data '{"firstName":"John","lastName":"Doe","street":"23 Main Street","city":"New York","state":"NY","zip":"10002","phone":"123-456-7890","email":"jdoe@mail.com","username":"jdoe","password":"asfsafd"}'	
	@RequestMapping(method = RequestMethod.POST, value="/register")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	public Account register(@RequestBody Account account) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
			conn = mainDataSource.createMainDataSource().getConnection();
			stmt = conn.prepareStatement("INSERT INTO ACCOUNT (CITY, EMAIL, FIRST_NAME, LAST_NAME, " + 
			                             "PHONE, STATE, STREET, USERNAME, PASSWORD, ZIP) VALUES (?,?,?,?,?,?,?,?,?,?)");
			stmt.setString(1, account.getCity());
			stmt.setString(2, account.getEmail());
			stmt.setString(3, account.getFirstName());
			stmt.setString(4, account.getLastName());
			stmt.setString(5, account.getPhone());
			stmt.setString(6, account.getState());
			stmt.setString(7, account.getStreet());
			stmt.setString(8, account.getUsername());
			stmt.setString(9, account.getPassword());
			stmt.setString(10, account.getZip());
			int i = stmt.executeUpdate();
			
			stmt.close();
			Account a = findAccount(conn, account.getUsername());
			conn.commit();
			return a;
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
	
// curl -X POST -H 'Content-Type: application/json' -i 'http://localhost:8081/getAccount' --data '{"firstName":"John","lastName":"Doe","street":"23 Main Street","city":"New York","state":"NY","zip":"10002","phone":"123-456-7890","email":"jdoe@mail.com","username":"jdoe","password":"asfsafd"}'	
	@RequestMapping(method = RequestMethod.POST, value="/getAccount")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	public Account getAccount(@RequestBody Account account) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
			return findAccount(conn, account.getUsername());
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
	
	
//  curl -X POST -H 'Content-Type: application/json' -i http://localhost:8081/updateAccount --data '{"firstName":"Joe","lastName":"Shmoe","street":"23 Main Street","city":"New York","state":"NY","zip":"10002","phone":"123-456-7890","email":"jdoe@mail.com","username":"jdoe","password":"asfsafd"}'	
	@RequestMapping(method = RequestMethod.POST, value="/updateAccount")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:3000")
	public int updateAccount(@RequestBody Account account) {
		Connection conn = null;
		PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
			conn = mainDataSource.createMainDataSource().getConnection();
        	Account fromDB = this.findAccount(conn, account.getUsername());
			stmt = conn.prepareStatement("update account set CITY = ?, EMAIL = ?, FIRST_NAME = ?, LAST_NAME = ?, " 
					+ "PHONE = ?, STATE = ?, STREET = ?, ZIP = ?, password = ? where upper(username) = upper(?)");
			stmt.setString(1, account.getCity());
			stmt.setString(2, account.getEmail());
			stmt.setString(3, account.getFirstName());
			stmt.setString(4, account.getLastName());
			stmt.setString(5, account.getPhone());
			stmt.setString(6, account.getState());
			stmt.setString(7, account.getStreet());
			stmt.setString(8, account.getZip());
			if (account.getPassword() != null && account.getPassword().trim().length() > 0) {
				stmt.setString(9, account.getPassword());
			}
			else {
				stmt.setString(9,  fromDB.getPassword());
			}
			stmt.setString(10, account.getUsername());
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
	
	private Account findAccount(Connection conn, String username) throws SQLException {
		PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
	        conn = mainDataSource.createMainDataSource().getConnection();
			stmt = conn.prepareStatement("select * from account where upper(username) = upper(?)");
			stmt.setString(1, username);
			rs = stmt.executeQuery();
			Account a = new Account();
			if (rs.next()) {
				a = toAccount(rs);
			}
			return a;
        } 
        finally {
        	DBCleanup.cleanup(null, stmt, rs);
        }
	}
}
