package com.tmoncorp.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class MySqlConnector {
	private BasicDataSource datasource;

	public void setDatasource(BasicDataSource datasource) {
		this.datasource = datasource;
	}

	public Connection getConnection() throws SQLException {
		return datasource.getConnection();
	}
}
