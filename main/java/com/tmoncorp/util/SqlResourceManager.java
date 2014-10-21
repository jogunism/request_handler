package com.tmoncorp.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqlResourceManager {

	public static void close(Connection conn) {
		if (conn == null) {
			return;
		}

		try {
			conn.close();
		} catch (Exception ignored) {
		}
	}

	public static void close(Statement stmt) {
		if (stmt == null) {
			return;
		}
		
		try {
			stmt.close();
		} catch (Exception ignored) {
		}
	}

	public static void close(ResultSet rset) {
		if (rset == null) {
			return;
		}
		
		try {
			rset.close();
		} catch (Exception ignored) {
		}
	}
}
