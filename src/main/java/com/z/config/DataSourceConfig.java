package com.z.config;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
	@Bean(name = "mainDataSource")
    public DataSource createMainDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:~/test;MODE=MySQL");
        ds.setUser("sa");
        ds.setPassword("");
        System.out.println("         -- mainDataSource -- initiated --   ");
        return ds;
    }
}
