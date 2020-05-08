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
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 public class DBConnector {
     private Connection mConnection;
     private DBConnector() throws Exception{
         Class.forName("org.sqlite.JDBC");
         mConnection = DriverManager.getConnection("jdbc:sqlite:mui.db");
         Statement stmt = mConnection.createStatement();
         // create all table
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tbl_company(" + 
                 "name      STRING PRIMARY KEY," +
                 "address    STRING);");
 
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tbl_certificate(" +
                 "number         INTEGER PRIMARY KEY," +
                 "product_type   STRING ," +
                 "product_name   STRING ," +
                 "issued         DATE," +
                 "valid_until    DATE," +
                 "url            STRING," +
                 "company_name   STRING," +
                 "FOREIGN KEY (company_name) REFERENCES tbl_company(name));");
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_product_type(" +
         		"certnumber INTEGER," +
         		"name STRING," +
         		"ulstatus INTEGER," +
         		"FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
         		"FOREIGN KEY(name) REFERENCES tbl_certificate(product_type));");
 
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_product_name(" +
                 "certnumber INTEGER," +
                 "name STRING," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
                 "FOREIGN KEY(name) REFERENCES tbl_certificate(product_name));");
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_issued(" +
                 "certnumber INTEGER," +
                 "issued DATE," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
                 "FOREIGN KEY(issued) REFERENCES tbl_certificate(issued));");
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_valid_until(" +
                 "certnumber INTEGER," +
                 "valid_until DATE," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
                 "FOREIGN KEY(valid_until) REFERENCES tbl_certificate(valid_until));");
         
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_url(" +
                 "certnumber INTEGER," +
                 "url        STRING," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
                 "FOREIGN KEY(url) REFERENCES tbl_certificate(url));");        
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_company_name(" +
                 "certnumber INTEGER," +
                 "company_name STRING," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number),"+
                 "FOREIGN KEY(company_name) REFERENCES tbl_certificate(company_name));");
         
         stmt.executeUpdate("CREATE TABLE IF NOT EXISTS uploadtask_company_address(" +
                 "certnumber INTEGER," +
                 "company_address STRING," +
                 "ulstatus INTEGER," +
                 "FOREIGN KEY(certnumber) REFERENCES tbl_certificate(number));");
         
         stmt.executeUpdate("CREATE VIEW IF NOT EXISTS uploadtask_view AS " +
         		"SELECT certnumber,name FROM uploadtask_product_type WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,name FROM uploadtask_product_name WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,issued FROM uploadtask_issued WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,valid_until FROM uploadtask_valid_until WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,url FROM uploadtask_url WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,company_name FROM uploadtask_company_name WHERE ulstatus = 0 UNION " +
         		"SELECT certnumber,company_address FROM uploadtask_company_address WHERE ulstatus = 0;");
         
         stmt.executeUpdate("CREATE TRIGGER IF NOT EXISTS trigger_certificate " +
         		"AFTER INSERT ON tbl_certificate " +
         		"BEGIN" +
         		"    INSERT INTO uploadtask_product_type VALUES (new.number,new.product_type,0);" +
         		"    INSERT INTO uploadtask_product_name VALUES (new.number,new.product_name,0);" +
         		"    INSERT INTO uploadtask_issued VALUES(new.number,new.issued,0);" +
                 "    INSERT INTO uploadtask_valid_until VALUES(new.number,new.valid_until,0);" +
                 "    INSERT INTO uploadtask_url VALUES(new.number,new.url,0);" +                
                 "    INSERT INTO uploadtask_company_name VALUES(new.number,new.company_name,0);" +
                 "    INSERT INTO uploadtask_company_address " +
                 "           SELECT tbl_certificate.number,tbl_company.address,0 " +
                 "           FROM tbl_certificate " +
                 "           LEFT JOIN tbl_company " +
                 "           ON tbl_company.name=tbl_certificate.company_name " +
                 "           WHERE tbl_certificate.number=new.number;" +                
         		"END;");
         
 
         stmt.close();
         
     }
     
     public Connection getConnector(){
         return mConnection;
     }
     
     private static DBConnector sDbConnector;
     
     public static Connection getConnection() throws Exception{
         if(sDbConnector == null) sDbConnector = new DBConnector();
         return sDbConnector.getConnector();
         
     }
 
 }
