package com.z.utils;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBCleanup {
    public static void cleanup(Connection conn, Statement stmt, ResultSet rs) {
    	try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
			}
			if (stmt != null && ! stmt.isClosed()) {
				stmt.close();
			}
			if (conn != null && ! conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
