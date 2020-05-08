 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2012 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author  adrianromero
  */
 public class StatementSelect<T> extends Statement {
 
     private ResultSet resultset;
    private KindResults kr = new KindResultsMap();
 
     public StatementSelect(Connection conn, String sql) {
         super(conn, sql);
     }
     public StatementSelect(Connection conn, String sql, String... params) {
         super(conn, sql, params);
     }
 
     private final class KindResultsMap implements KindResults {
         @Override
         public Integer getInt(int columnIndex) throws SQLException {
             int iValue = resultset.getInt(columnIndex);
             return resultset.wasNull() ? null : new Integer(iValue);
         }
         @Override
         public Integer getInt(String columnName) throws SQLException {
             int iValue = resultset.getInt(columnName);
             return resultset.wasNull() ? null : new Integer(iValue);
         }
         @Override
         public String getString(int columnIndex) throws SQLException {
             return resultset.getString(columnIndex);
         }
         @Override
         public String getString(String columnName) throws SQLException {
             return resultset.getString(columnName);
         }
         @Override
         public Double getDouble(int columnIndex) throws SQLException {
             double dValue = resultset.getDouble(columnIndex);
             return resultset.wasNull() ? null : new Double(dValue);
         }
         @Override
         public Double getDouble(String columnName) throws SQLException {
             double dValue = resultset.getDouble(columnName);
             return resultset.wasNull() ? null : new Double(dValue);
         }
         @Override
         public Boolean getBoolean(int columnIndex) throws SQLException {
             boolean bValue = resultset.getBoolean(columnIndex);
             return resultset.wasNull() ? null : bValue;
         }
         @Override
         public Boolean getBoolean(String columnName) throws SQLException {
             boolean bValue = resultset.getBoolean(columnName);
             return resultset.wasNull() ? null : bValue;
         }
         @Override
         public java.util.Date getTimestamp(int columnIndex) throws SQLException {
             java.sql.Timestamp ts = resultset.getTimestamp(columnIndex);
             return ts == null ? null : new java.util.Date(ts.getTime());
         }
         @Override
         public java.util.Date getTimestamp(String columnName) throws SQLException {
             java.sql.Timestamp ts = resultset.getTimestamp(columnName);
             return ts == null ? null : new java.util.Date(ts.getTime());
         }
         @Override
         public java.util.Date getDate(int columnIndex) throws SQLException {
             java.sql.Date da = resultset.getDate(columnIndex);
             return da == null ? null : new java.util.Date(da.getTime());
         }
         @Override
         public java.util.Date getDate(String columnName) throws SQLException {
             java.sql.Date da = resultset.getDate(columnName);
             return da == null ? null : new java.util.Date(da.getTime());
         }
         @Override
         public java.util.Date getTime(int columnIndex) throws SQLException {
             java.sql.Time da = resultset.getTime(columnIndex);
             return da == null ? null : new java.util.Date(da.getTime());
         }
         @Override
         public java.util.Date getTime(String columnName) throws SQLException {
             java.sql.Time da = resultset.getTime(columnName);
             return da == null ? null : new java.util.Date(da.getTime());
         }        
         @Override
         public byte[] getBytes(int columnIndex) throws SQLException {
             return resultset.getBytes(columnIndex);
         }
         @Override
         public byte[] getBytes(String columnName) throws SQLException {
             return resultset.getBytes(columnName);
         }
     }
 
     @Override
     protected void openStmt() throws SQLException {
         resultset = stmt.executeQuery();
     }
 
     @Override
     protected void closeStmt() throws SQLException {
         resultset.close();
         resultset = null;
      }
 
     public final List<T> readall(Results<T> results) throws SQLException {
 
         List<T> l = new ArrayList<T>();
         while (resultset.next()) {
             l.add(results.read(kr));
         }
         return l;
     }
 
     public final T read(Results<T> results) throws SQLException {
 
         if (resultset.next()) {
             return results.read(kr);
         } else {
             return null;
         }
     }
 
     public final T find(Results<T> results) throws SQLException {
         open();
         return readAndClose(results);
     }
 
     public final T find(Results<T> results, Parameters params) throws SQLException {
         open(params);
         return readAndClose(results);
     }
 
     public final T find(Results<T> results, String... param) throws SQLException {
         open(param);
         return readAndClose(results);
     }
 
     public final T find(Results<T> results, KindValue... param) throws SQLException {
         open(param);
         return readAndClose(results);
     }
 
     private T readAndClose(Results<T> results) throws SQLException {
         T value = read(results);
         close();
         return value;
     }
 
     public final List<T> list(Results<T> results) throws SQLException {
         open();
         return readallAndClose(results);
     }
 
     public final List<T> list(Results<T> results, Parameters params) throws SQLException {
         open(params);
         return readallAndClose(results);
     }
 
     public final List<T> list(Results<T> results, String... param) throws SQLException {
         open(param);
         return readallAndClose(results);
     }
 
     public final List<T> list(Results<T> results, KindValue... param) throws SQLException {
         open(param);
         return readallAndClose(results);
     }
 
     private List<T> readallAndClose(Results<T> results) throws SQLException {
         List<T> values = readall(results);
         close();
         return values;
     }
 }
 
