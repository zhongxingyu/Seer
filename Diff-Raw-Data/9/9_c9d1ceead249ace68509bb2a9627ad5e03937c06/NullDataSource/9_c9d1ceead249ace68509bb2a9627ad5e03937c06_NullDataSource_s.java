 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 
 package org.lafayette.server.domain.db;
 
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.util.logging.Logger;
 import javax.sql.DataSource;
 
 /**
  * A default implementation of {@link DataSource} which does nothing for testing.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class NullDataSource implements DataSource {
 
     @Override
     public Connection getConnection() throws SQLException {
         return new NullConnection();
     }
 
     @Override
     public Connection getConnection(String username, String password) throws SQLException {
         return new NullConnection();
     }
 
     @Override
     public PrintWriter getLogWriter() throws SQLException {
         return null;
     }
 
     @Override
     public void setLogWriter(PrintWriter out) throws SQLException {
 
     }
 
     @Override
     public void setLoginTimeout(int seconds) throws SQLException {
 
     }
 
     @Override
     public int getLoginTimeout() throws SQLException {
         return 0;
     }
 
     @Override
     public <T> T unwrap(Class<T> iface) throws SQLException {
         return null;
     }
 
     @Override
     public boolean isWrapperFor(Class<?> iface) throws SQLException {
         return false;
     }
 
     @Override
     public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return java.util.logging.Logger.getAnonymousLogger();
     }
 
 }
