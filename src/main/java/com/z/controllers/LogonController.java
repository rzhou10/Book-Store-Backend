package com.z.controllers;

import com.z.config.*;
import com.z.utils.DBCleanup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.z.ErrorBean;
import com.z.beans.Account;

@RestController
public class LogonController implements ControllerHelper {
	@Autowired
	DataSourceConfig mainDataSource;
	
	@RequestMapping(value = "/validate", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ErrorBean> validateUser(@QueryParam("jsonInput") final String jsonInput) {
	    int numberHTTPDesired = 400;
	    ErrorBean responseBean = new ErrorBean();
	    responseBean.setStatus("ERROR");
	    responseBean.setMessage("Error in validating " + jsonInput + " in book project!");

	    return new ResponseEntity<ErrorBean>(responseBean, HttpStatus.valueOf(numberHTTPDesired));
	}
	
// curl -X POST -H 'Content-Type: application/json' -i http://localhost:8081/logon --data '{"username":"jdoe","password":"asfjdksfjd"}'	
	  @RequestMapping(method = RequestMethod.POST, value="/logon")
	  @ResponseBody
	  @CrossOrigin(origins = "http://localhost:3000")
	  public Account logon(@RequestBody Account account) {

            System.out.println("username: " + account.getUsername());
            if (account.getUsername() == null || account.getPassword() == null) {
	            return account;
            }
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
				conn = mainDataSource.createMainDataSource().getConnection();
				stmt = conn.prepareStatement("select * from account where upper(username) = upper(?) and password = ?");
				stmt.setString(1, account.getUsername());
				stmt.setString(2, account.getPassword());
				rs = stmt.executeQuery();
				System.out.println(rs);
				if (rs.next()) {
					return toAccount(rs);
				}
				else {
					throw new ResponseStatusException(
					          HttpStatus.NO_CONTENT, "Incorrect username/password combination");
				}
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
}
