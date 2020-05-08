 package org.lightmare.jpa.datasource;
 
 /**
  * Contains error messages for data source initializations
  * 
  * @author Levan
  * 
  */
 public class InitMessages {
 
     // Error messages
     public static final String NOT_APPR_INSTANCE_ERROR = "Could not initialize data source %s (it is not appropriated DataSource instance)";
 
     public static final String COULD_NOT_INIT_ERROR = "Could not initialize data source %s";
 
     public static final String COULD_NOT_CLOSE_ERROR = "Could not close DataSource %s";
 
     // Info Messages
     public static final String INITIALIZING_MESSAGE = "Initializing data source %s";
 
    public static final String INITIALIZED_MESSAGE = "Data source %s initialized";

 }
