 /*
  * Copyright (C) 2010 The Halal Certification Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package com.mui.certificate.core.db;
 
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class TempCertNumber {
     private Statement mStmt;
     public TempCertNumber(Connection connection) throws SQLException{
         mStmt = connection.createStatement();
     }
 
     /**
      * Check our temporary table whether the table is empty or not
      * @throws SQLException 
      * */
     public boolean isEmpty() throws SQLException{
        ResultSet rs = mStmt.executeQuery("SELECT * FROM tbl_temp_certificate;");
        if(rs.next()) 
            return false;
        
        return true;
     }
     /**
      * Get certificate number
      * @throws SQLException 
      * */
     public Integer getNumber() throws SQLException{
        ResultSet rs = mStmt.executeQuery("SELECT number FROM tbl_temp_certificate;");
         if(rs.next()){
             return rs.getInt(1);
         }
         return null;
     }
     
     public URL getCertURL()throws Exception{
        ResultSet rs = mStmt.executeQuery("SELECT url FROM tbl_temp_certificate;");
         if(rs.next()){
             return new URL(rs.getString(1));
         }
        return null;
     }
     
     
     public void setTempCert(Integer number,URL url) throws SQLException{
        mStmt.executeUpdate("INSERT INTO tbl_temp_certificate VALUES( "+
                 number.toString() +",\'"+url.toString()+"\' );");        
     }
     
     public void close() throws SQLException{
         mStmt.close();
     }
     
 }
