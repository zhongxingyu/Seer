 package org.mattyo161.commons.db;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.lang.*;
 import java.sql.*;
 import org.mattyo161.commons.util.MyEnvironment;
 
 /**
  *A factory for creating JDBC Connections. The user only needs to now the database type that
  *they want to connect to and the ConnectionFactory will choose the appropriate Connectoin2xxx
  *class that will handle all of the JDBC Driver and URL details.
  *
  *The connection factory is used in the following manner:
  *Connection conn = ConnectionFactory.getConnection(args)
  *
  *Currently the following databases are supported:
  *mssql65, mssql7, mssql2000, mysql and sybase
  *
  *For future development the following is source code for a Connection2xxx class:
  *This is an example for a sybase database, you would need to replace the instances
  *of sybase as well as modify the values for dbDriver and dbURL appropriately otherwise
  *the code should be able to list as is.
  *
  *<pre>
 class Connection2sybase implements IConnection2Db
 {
     public Connection getConnection(String server,
             String port,
             String user,
             String password,
             String db,
             String options)
     {
         Connection conn = null;
 
         try {
             String dbDriver = "com.sybase.jdbc2.jdbc.SybDriver";
             String dbURL = "jdbc:sybase:Tds:" + server;
             if (!port.equals("")) {
                 dbURL += ":" + port;
             }
             dbURL += "/" + db;
             if (!options.equals("")) {
                 dbURL += ";" + options;
             }
 
             // Try to create a new instance of the driver
             if (dbDriver != "") {
                 try
                 {
                    Class.forName(dbDriver).newInstance();
                 } catch (Exception e) {
                     String errorString = "Unable to Load JDBC Driver:\n" +
                         "DriverClass: " + dbDriver + "\n" +
                         "Error: " + e.toString();
                     throw new Exception(errorString);
                 }
             }
 
             // get a new connection object
             if (dbDriver != "" && dbURL != "") {
                 try {
                     conn = DriverManager.getConnection(dbURL, user, password);
                 } catch (Exception e) {
                     String errorString = "Unable to Connect to JDBC Driver:\n" +
                         "DriverClass: " + dbDriver + "\n" +
                         "URL: " + dbURL + "\n" +
                         "Error: " + e.toString();
                     throw new Exception(errorString);
                 }
             }
 
         } catch (Exception e) {
             //throw e;
             e.printStackTrace();
         }
 
         return conn;
     }
 
     public Connection getConnection(String server, String port, String user, String password) {
         return getConnection(server, port, user, password, "", "");
     }
 
     public Connection getConnection(String server, String port, String user, String password, String db) {
         return getConnection(server, port, user, password, db, "");
     }
 
 }
  *</pre>
  *
  * @author Matt Ouellette
  * @version 1.00 March 11, 2004
  */
 
 public class ConnectionFactory {
     // Prevent the creation of an object from this class
     private ConnectionFactory() {}
 
     /**
      *<p>Establish a connection to a database based on a given database source &lt;src&gt; or dburl.
      *Sources are defined in the environment for the given program this could be a unix
      *shell, tomcat env variables or application preferences, currently only tomcat
      *env variables are supported.</p>
      *
      *<p>The variables must begin with &lt;src&gt; and can have the following
      *additional settings</p>
      *<pre>
      *&lt;src&gt;_serverType (required) = the ServerType (mysql, mssql65, sybase, etc.)
      *&lt;src&gt;_server (required) = the server ip or dns name
      *&lt;src&gt;_port = the port the db listens to, blank will set it to the
      *  default port for the database
      *&lt;src&gt;_user (required) = the user to connect to the database
      *&lt;src&gt;_password (required) = the password for the specified user
      *&lt;src&gt;_db = the database to use
      *&lt;src&gt;_options = any additional JDBC options that might be needed
      *</pre>
      *Or you could use a single dburl entry
      *<pre>
      *&lt;src&gt;_dburl = <serverType>://<user>[:<password>]@<server>:<port>[/<db>][;<options>]
      *<pre>
      *
      *<p>If you have a source named "mydb" configured you simply call the following to create
      *a connection.</p>
      *<pre>
      *try {
      *  Connection conn = ConnectionFactory.getConnection("mydb")
      *} catch (Exception e) {
      *  System.out.println(e.toString());
      *  e.printStackTrace();
      *}
      *</pre>
      *Based on the serverType the Connection factory will pick the appropriate Connection2xxx
      *class and generate the Connection object.
      *
      *You also have the ability to call getConnection with the URL syntax described above
      *so instead of specifying a single source name you can enter the url like so
      *<pre>
      *try {
      *  Connection conn = ConnectionFactory.getConnection("mysql://test:pass@myserver/testdb");
      *} catch (Exception e) {
      *  System.out.println(e.toString());
      *  e.printStackTrace();
      *}
      *
      *@param src the source name stored in environment or a valid dburl
      *@return a Connection object to the given database
      *@throws Exception if there is an error generating a Connection object
      */
 
     public static Connection getConnection(String src)
             throws Exception
     {
         Connection conn = null;
         MyEnvironment env = null;
 
         try {
             if (src.matches("^\\w+://.+")) {
                 conn = getConnectionFromUrl(src);
             } else if (src.toLowerCase().startsWith("dp.")) {
             		// this is a database pool connection so we want to use the Connection manager
             		conn = ConnectionManager.getConnection(src.substring(3));
             } else {
                 env = MyEnvironment.getEnvironment();
                 // get the list of properties and see if there is a serverType for the given source
                 ArrayList envKeys = env.getKeys();
                 if (envKeys.contains(src + "_dburl")) {
                     conn = getConnectionFromUrl(env.get(src + "_dburl"));
                 } else if (envKeys.contains(src + "_serverType")) {
                         String connClass = "org.mattyo161.commons.db.Connection2" + env.get(src + "_serverType");
                         IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
                         conn = tmpClass.getConnection(
                             env.get(src + "_server"),
                             env.get(src + "_port"),
                             env.get(src + "_user"),
                             env.get(src + "_password"),
                             env.get(src + "_db"),
                             env.get(src + "_options")
                             );
                 }
             }
         } catch (Exception e) {
             throw e;
         }
         return conn;
     }
 
     public static ConnectionProperties getConnectionProps(String src) {
         ConnectionProperties props = new ConnectionProperties();
         MyEnvironment env = null;
 
         if (src.matches("^\\w+://.+")) {
             props = getConnectionPropsFromUrl(src);
         } else if (src.toLowerCase().startsWith("dp.")) {
         		// we need to look up the props for the config entry for the given database pool entry.
         		props = getConnectionProps(ConnectionManagerConfig.config().get(src.substring(3)));
         } else {
             env = MyEnvironment.getEnvironment();
             // get the list of properties and see if there is a serverType for the given source
             ArrayList envKeys = env.getKeys();
             if (envKeys.contains(src + "_dburl")) {
                 props = getConnectionPropsFromUrl(env.get(src + "_dburl"));
             } else if (envKeys.contains(src + "_serverType")) {
                     props.setServerType(env.get(src + "_serverType"));
                     props.setServer(env.get(src + "_server"));
                     props.setPort(env.get(src + "_port"));
                     props.setUser(env.get(src + "_user"));
                     props.setPassword(env.get(src + "_password"));
                     props.setDb(env.get(src + "_db"));
                     props.setOptions(env.get(src + "_options"));
                     String jdbcDriver = getJdbcDriver(props.getServerType());
                     String jdbcUrl = getJdbcUrl(props.getServerType(), props.getServer(), props.getPort(), props.getDb(), props.getOptions());
                     props.setJdbcDriver(jdbcDriver);
                     props.setJdbcUrl(jdbcUrl);
             } else {
             		// lets see if this is an old style configuration pool setting
 	    			String[] value = src.split("!");
 	    			if (value.length == 4) {
 		    		    String classname = value[0];
 		    		    String url = value[1];
 		    		    String user = value[2];
 		    		    String passwd = value[3];
 		    		    props.setPassword(passwd);
 		    		    props.setUser(user);
 		    		    props.setJdbcUrl(url);
 		    		    props.setJdbcDriver(classname);
 	    			}
             }
         }
         return props;
     }
 
     /**
      * Establishes a Connection to a database based on a given database URL in the form
      * <pre><server_type>://<user>:<password>@<server>/<db>;<options></pre>
      */
 
     private static Connection getConnectionFromUrl(String dbUrl)
         throws Exception {
         Connection conn = null;
 
         // Frist we need to extract the ServerType from the rest of the Url
         int pos;
         // Initialize the connection variables
         String serverType = "";
         String server = "";
         String port = "";
         String user = "";
         String password = "";
         String db = "";
         String options = "";
 
         // Get the connection variables
         ConnectionProperties dbUrlProps = getConnectionPropsFromUrl(dbUrl);
         serverType = dbUrlProps.getServerType();
         server = dbUrlProps.getServer();
         port = dbUrlProps.getPort();
         user = dbUrlProps.getUser();
         password = dbUrlProps.getPassword();
         db = dbUrlProps.getDb();
         options = dbUrlProps.getOptions();
 
         if (false) {
 	        pos = dbUrl.indexOf("://");
 	        if (pos > 0) {
 	            serverType = dbUrl.substring(0,pos);
 	            // set dbUrl to the remainder of the string
 	            dbUrl = dbUrl.substring(pos + 3);
 	            // now we need to extract the server info from the dbinfo and options
 	            pos = dbUrl.indexOf("/");
 	            if (pos > 0) {
 	                server = dbUrl.substring(0,pos);
 	                if (pos < dbUrl.length()) {
 	                    db = dbUrl.substring(pos + 1);
 	                } else {
 	                    db = "";
 	                }
 	            } else {
 	                server = dbUrl;
 	            }
 	            pos = server.indexOf("@");
 	            if (pos > 0) {
 	                user = server.substring(0, pos);
 	                if (pos < server.length()) {
 	                    server = server.substring(pos + 1);
 	                } else {
 	                    server = "";
 	                }
 	                pos = server.indexOf(":");
 	                if (pos > 0) {
 	                    if (pos < server.length()) {
 	                        port = server.substring(pos + 1);
 	                    } else {
 	                        port = "";
 	                    }
 	                    server = server.substring(0, pos);
 	                }
 	                pos = user.indexOf(":");
 	                if (pos > 0) {
 	                    if (pos < user.length()) {
 	                        password = user.substring(pos + 1);
 	                    } else {
 	                        password = "";
 	                    }
 	                    user = user.substring(0, pos);
 	                }
 	            }
 	            pos = db.indexOf(";");
 	            if (pos > 0) {
 	                if (pos < db.length()) {
 	                    options = db.substring(pos + 1);
 	                } else {
 	                    options = "";
 	                }
 	                db = db.substring(0, pos);
 	            }
 	        }
         }
 
 
         // If we have a serverType, server, and user then we have enough to make a connection
         // access and possibly some other dbs do not require server or user information so we can skip that check
         // if (!(serverType.equals("") || server.equals("") || user.equals(""))) {
         if (!(serverType.equals(""))) {
             // Try to create a connection to the given serverType
             try {
                 String connClass = "org.mattyo161.commons.db.Connection2" + serverType;
                 IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
                 conn = tmpClass.getConnection(
                     server,
                     port,
                     user,
                     password,
                     db,
                     options
                     );
             } catch (Exception e) {
                 throw e;
             }
         } else {
             String errorString = "Unable to Load JDBC Driver:\n" +
                 "Invalid URL: " + dbUrl + "\n";
             if (serverType.equals("")) errorString += "Missing: serverType\n";
             if (server.equals("")) errorString += "Missing: server\n";
             if (user.equals("")) errorString += "Missing: user\n";
 
             throw new Exception(errorString);
         }
         return conn;
     }
 
     private static ConnectionProperties getConnectionPropsFromUrl(String dbUrl) {
 
         // Frist we need to extract the ServerType from the rest of the Url
         int pos;
         // initialize connection variables
         String serverType = "";
         String server = "";
         String port = "";
         String user = "";
         String password = "";
         String db = "";
         String options = "";
         pos = dbUrl.indexOf("://");
         if (pos > 0) {
             serverType = dbUrl.substring(0,pos);
             // set dbUrl to the remainder of the string
             dbUrl = dbUrl.substring(pos + 3);
             // now we need to extract the server info from the dbinfo and options
             pos = dbUrl.indexOf("/");
             if (pos > 0) {
                 server = dbUrl.substring(0,pos);
                 if (pos < dbUrl.length()) {
                     db = dbUrl.substring(pos + 1);
                 } else {
                     db = "";
                 }
             } else {
                 server = dbUrl;
             }
             pos = server.indexOf("@");
             if (pos > 0) {
                 user = server.substring(0, pos);
                 if (pos < server.length()) {
                     server = server.substring(pos + 1);
                 } else {
                     server = "";
                 }
                 pos = server.indexOf(":");
                 if (pos > 0) {
                     if (pos < server.length()) {
                         port = server.substring(pos + 1);
                     } else {
                         port = "";
                     }
                     server = server.substring(0, pos);
                 }
                 pos = user.indexOf(":");
                 if (pos > 0) {
                     if (pos < user.length()) {
                         password = user.substring(pos + 1);
                     } else {
                         password = "";
                     }
                     user = user.substring(0, pos);
                 }
             }
             pos = db.indexOf(";");
             if (pos > 0) {
                 if (pos < db.length()) {
                     options = db.substring(pos + 1);
                 } else {
                     options = "";
                 }
                 db = db.substring(0, pos);
             }
         }
         ConnectionProperties props = new ConnectionProperties();
         props.setServerType(serverType);
         props.setServer(server);
         props.setPort(port);
         props.setUser(user);
         props.setPassword(password);
         props.setDb(db);
         props.setOptions(options);
         String jdbcDriver = getJdbcDriver(props.getServerType());
         String jdbcUrl = getJdbcUrl(props.getServerType(), props.getServer(), props.getPort(), props.getDb(), props.getOptions());
         props.setJdbcDriver(jdbcDriver);
         props.setJdbcUrl(jdbcUrl);
 
         return props;
     }
 
     /**
      *<p>Establishes a JDBC Connection to a database based on the arguments passed.</p>
      *
      *<pre>
      *serverType (required) - the type of server you want to connect to
      *  (The following databases are currently suppored)
      *  mssql65 - Microsoft SQL Server 6.5
      *  mssql7 - Microsoft SQL Server 7
      *  mssql2000 - Microsoft SQL Server 2000
      *  mysql - MySQL
      *  sybase - Sybase ASE 12.5, may be backwards compatible
      *server (required) - the ip or dns name for the server being connected to
      *port - the port to connect to on the server (empty string chooses default port)
      *user (required) - the user to connect as on the server
      *password (required) - the password for the user
      *db - the name of the database to connect to
      *options - additional JDBC parameters for driver customization
      *</pre>
      *
      *<p>Here is an example of creating a Connection to a MySql server running on the
      *machine myserver using user "me" and password "pass", no database will be selected
      *it will used the default MySql port and no additional options are specified</p>
      *<pre>
      *try {
      *  Connection conn = ConnectionFactory.getConnection("mysql","myserver","","me","pass","","")
      *} catch (Exception e) {
      *  System.out.println(e.toString());
      *  e.printStackTrace();
      *}
      *</pre>
      *<p>Based on the serverType the Connection factory will pick the appropriate Connection2xxx
      *class and generate the Connection object.</p>
      *
      *@param serverType the type of server to connect to ex. (mysql)
      *@param server the ip or name of the server to connect to
      *@param port the port on the server to connect to, empty string or null, will choose default port for type of server
      *@param user the user to connect to the server
      *@param password password for user
      *@param db name of the database to use in the connection, blank will not use a database, must be specified later
      *@param options additional JDBC options, this is driver specific, use with caution
      *@return a Connection object to the given database
      *@throws Exception if there is an error generating a Connection object
      */
 
     public static ConnectionProperties getConnectionProps(String serverType,
             String server,
             String port,
             String user,
             String password,
             String db,
             String options)
     {
         ConnectionProperties props = new ConnectionProperties();
 
         props.setServerType(serverType);
         props.setServer(server);
         props.setPort(port);
         props.setUser(user);
         props.setPassword(password);
         props.setDb(db);
         props.setOptions(options);
         String jdbcDriver = getJdbcDriver(props.getServerType());
         String jdbcUrl = getJdbcUrl(props.getServerType(), props.getServer(), props.getPort(), props.getDb(), props.getOptions());
         props.setJdbcDriver(jdbcDriver);
         props.setJdbcUrl(jdbcUrl);
 
 
         return props;
     }
 
     public static Connection getConnection(String serverType,
             String server,
             String port,
             String user,
             String password,
             String db,
             String options)
             throws Exception
     {
         Connection conn = null;
 
         try {
             String connClass = "org.mattyo161.commons.db.Connection2" + serverType;
             IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
             conn = tmpClass.getConnection(
                 server,
                 port,
                 user,
                 password,
                 db,
                 options
                 );
         } catch (Exception e) {
             throw e;
         }
 
         return conn;
     }
 
     /**
      * Same as getConnection only the options, argument is left out
      *
      *@param serverType the type of server to connect to ex. (mysql)
      *@param server the ip or name of the server to connect to
      *@param port the port on the server to connect to, empty string or null, will choose default port for type of server
      *@param user the user to connect to the server
      *@param password password for user
      *@param db name of the database to use in the connection, blank will not use a database, must be specified later
      *@return a Connection object to the given database
      *@throws Exception if there is an error generating a Connection object
      */
 
     public static ConnectionProperties getConnectionProps(String serverType,
             String server,
             String port,
             String user,
             String password,
             String db)
     {
         return getConnectionProps(serverType, server, port, user, password, db, "");
     }
 
     public static Connection getConnection(String serverType,
             String server,
             String port,
             String user,
             String password,
             String db)
             throws Exception
     {
         return getConnection(serverType, server, port, user, password, db, "");
     }
 
     /**
      * Same as getConnection only the db and options, argument are left out
      *
      *@param serverType the type of server to connect to ex. (mysql)
      *@param server the ip or name of the server to connect to
      *@param port the port on the server to connect to, empty string or null, will choose default port for type of server
      *@param user the user to connect to the server
      *@param password password for user
      *@return a Connection object to the given database
      *@throws Exception if there is an error generating a Connection object
      */
 
     public static ConnectionProperties getConnectionProps(String serverType,
             String server,
             String port,
             String user,
             String password)
     {
         return getConnectionProps(serverType, server, port, user, password, "", "");
     }
 
     public static Connection getConnection(String serverType,
             String server,
             String port,
             String user,
             String password)
             throws Exception
     {
         return getConnection(serverType, server, port, user, password, "", "");
     }
 
     /**
      * Returns a string array of the Strings used to quote a field in the sql syntax
      * @param serverType
      * @return
      */
     public static String[] getQuoting(String serverType) {
         String[] returnValue = new String[] {"",""};
         try {
             String connClass = "org.mattyo161.commons.db.Connection2" + serverType;
             IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
             returnValue = tmpClass.getQuoting();
         } catch (Exception e) {
             System.err.println(e);
         }
         return returnValue;
     }
 
     /**
      * Return the jdbc driver used to access the database
      *@param serverType the type of server to connect to ex. (mysql)
      * @return
      */
     public static String getJdbcDriver(String serverType) {
         String returnValue = "";
         try {
             String connClass = "org.mattyo161.commons.db.Connection2" + serverType;
             IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
             returnValue = tmpClass.getJdbcDriver();
         } catch (Exception e) {
             System.err.println(e);
         }
         return returnValue;
     }
 
     /**
      * Return the jdbc driver used to access the database
      *@param dbUrl a valid ConnectionFactory url for sepecifying a database
      * @return
      */
     public static String getJdbcDriverFromUrl(String dbUrl) {
         String returnValue = "";
         try {
             ConnectionProperties dbUrlProps = getConnectionPropsFromUrl(dbUrl);
             returnValue = dbUrlProps.getJdbcDriver();
         } catch (Exception e) {
             System.err.println(e);
         }
         return returnValue;
     }
 
     /**
      * Return the url used to access the database
      *@param serverType the type of server to connect to ex. (mysql)
      *@param server the ip or name of the server to connect to
      *@param port the port on the server to connect to, empty string or null, will choose default port for type of server
      *@param db name of the database to use in the connection, blank will not use a database, must be specified later
      *@param options additional JDBC options, this is driver specific, use with caution
      * @return
      */
 	public static String getJdbcUrl(String serverType, String server, String port, String db, String options)
 	{
         String returnValue = "";
         try {
             String connClass = "org.mattyo161.commons.db.Connection2" + serverType;
             IConnection2Db tmpClass = (IConnection2Db) Class.forName(connClass).newInstance();
             returnValue = tmpClass.getJdbcUrl(server, port, db, options);
         } catch (Exception e) {
             System.err.println(e);
         }
         return returnValue;
 	}
 
     /**
      * Return the url used to access the database
      *@param dbUrl a valid ConnectionFactory url for sepecifying a database
      * @return
      */
 	public static String getJdbcUrlFromUrl(String dbUrl)
 	{
         String returnValue = "";
         try {
             ConnectionProperties dbUrlProps = getConnectionPropsFromUrl(dbUrl);
             returnValue = dbUrlProps.getJdbcUrl();
         } catch (Exception e) {
             System.err.println(e);
         }
         return returnValue;
 	}
 
 }
