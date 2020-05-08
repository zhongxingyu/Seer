 package com.uah.commons;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 /**
  *
  * @author Pablo Sicilia
  * @version Mavsel Tool 1.0
  */
 public class DataBaseManagement {
     /**************************************************************************
     *                              ATTRIBUTES
     **************************************************************************/
     protected static Connection connection;
     protected DataBaseConnectionParameters dbParams;
     protected String LMStype;
     
     
     
     /**************************************************************************
     *                              PUBLIC METHODS
     ***************************************************************************/  
    
     /**
      * 
      * @param dbConnectionParams 
      */  
    public void initDataBaseConnection(DataBaseConnectionParameters dbConnectionParams){
         
         try{               
             // Configuring database
             this.dbParams  = dbConnectionParams;
         
             // Loading driver for mysql-connector-java-5.1.16-bin
             Class.forName("com.mysql.jdbc.Driver");
             
             // Connecting to database
             connection = DriverManager.getConnection( "jdbc:mysql://"+dbParams.getDataBaseURL()+":"+dbParams.getDataBasePort()+"/"+dbParams.getDataBaseName()+"",
             dbParams.getDataBaseUser() , dbParams.getDataBaseUserPass());                              
             
         }catch( Exception e ) {
             e.printStackTrace();
         }              
     }
         
     
     /**
      * 
      * @param dbConnectionParams 
      */
     public void initDataBaseConnection(Connection dbConnection){
         
         try{               
            
             // Loading driver for mysql-connector-java-5.1.16-bin
             Class.forName("com.mysql.jdbc.Driver");
             
             // Connecting to database
             this.connection = dbConnection;
                                        
             
         }catch( Exception e ) {
             e.printStackTrace();
         }              
     }
     
     /**
      * closeConnection
      */
     public void closeConnection(){
        try{
             // Close connection            
             connection.close();
         }catch( Exception e ) {
             e.printStackTrace();
         }  
     }
 
     
     
     
 }
