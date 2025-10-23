package com.ssg.jspboard.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;

public enum DBConnection {
  INSTANCE;

  private HikariDataSource ds;

  DBConnection() {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    config.setJdbcUrl("jdbc:mysql://localhost:3306/sqldb?serverTimezone=Asia/Seoul&charEncoding=UTF-8");
    config.setUsername("root");
    config.setPassword("mysql1234");

    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit","2048");

    ds = new HikariDataSource(config);
  }

  // ConnectionUtil.INSTANCE.getConnection() 으로 Connection을 얻을 수 있도록 구성함.
  public Connection getConnection() throws Exception {
    return ds.getConnection();
  }
}
