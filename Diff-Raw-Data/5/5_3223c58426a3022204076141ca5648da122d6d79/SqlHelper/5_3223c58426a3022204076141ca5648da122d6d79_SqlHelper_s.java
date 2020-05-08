 package sw3.server;
 
 import java.sql.*;
 
 /**
  * Strongly influenced by the SqlManager class in sw6.GirafPlaceServer, this
  * class serves to ease MySQL connection management and query execution.
  * It's important to note that an instance of this class maintains an open
  * connection to the MySQL database it targets; it does not open the connection
  * on a per-transaction basis (... yet, at least).
  * @author Johannes
  */
 public class SqlHelper
 {
     public java.sql.Connection connection;
     
     private String _hostname;
     
     private int _port;
     
     private String _username;
     
     private String _password;
     
     private String _database;
     
     /**
      * Creates a new inactive SqlHelper instance.
      */
     public SqlHelper()
     {
         return;
     }
     
     /**
      * Creates a new SqlHelper instance and connects to a MySQL database on port
      * 3396 with the passed credentials..
      * @param host  Hostname or IP address to connect to.
      * @param user  Username to utilise.
      * @param pass  Password for the user.
      * @param database  Database on the server to open.
      * @throws SQLException
      */
     public SqlHelper(String host, String user, String pass, String database) throws SQLException
     {
         this(host, 3306, user, pass, database);
     }
     
     /**
      * Creates a new SqlHelper instance and connects to the server and
      * database passed in connectionString.
      * @param connectionString Valid Java jdbc connection string.
      * @param user  Username to utilise.
      * @param pass  Password for the user.
      * @throws SQLException 
      */
     public SqlHelper(String connectionString, String user, String pass) throws SQLException
     {
         this();
         parseUrl(connectionString);
         _username = user;
         _password = pass;
         connect();
     }
     
     /**
      * Creates a new SqlHelper instance and connects to a MySQL database with
      * the passed information.
      * @param host  Hostname or IP address to connect to.
      * @param port  Which port to connect to. Default it 3306.
      * @param user  Username to utilise.
      * @param pass  Password for the user.
      * @param database  Database on the server to open.
      * @throws SQLException
      */
     public SqlHelper(String host, int port, String user, String pass, String database) throws SQLException
     {
         this();
         
        _hostname = user;
         _port = port;
         _username = user;
         _password = pass;
         _database = database;
         
         connect();
     }
     
     /**
      * Attempts to parse a JDBC connection string (URL) into its smaller pieces
      * for local storage.
      * @param url The URL to attempt to parse.
      */
     public void parseUrl(String url)
     {
         // TODO: Test this method. I'm pretty sure it stores some wrong chars.
         _hostname = url.substring(url.indexOf("://"), url.lastIndexOf(':'));
         _database = url.substring(url.lastIndexOf('/')+1);
         _port = Integer.parseInt(url.substring(url.indexOf(":"), url.lastIndexOf('/')));
     }
     
     /**
      * Concatenates a connection string from the details given at construction.
      * @return
      */
     public String getUrl()
     {
         return "jdbc:mysql://" + _hostname + ":" + _port + "/" + _database;
     }
     
     /**
      * Connects to the database if the connection is not active.
      * @return
      */
     public Connection connect()
     {
         try
         {
             // If the connection is valid, return it.
            if (connection.isValid(7))
             {
                 return connection;
             }
             else
             {
                 connection = DriverManager.getConnection(getUrl(), _username, _password);
                 return connection;
             }
         } catch (SQLException e)
         {
             // I've no clue what exception would come from being this caucious,
             // but there it is.
             e.printStackTrace();
             // Let me re-iterate. This should *never* happen!
             return null;
         }
     }
     
     /**
      * Closes the active connection object or just nulls it out.
      */
     public void disconnect()
     {
         try
         {
             connection.close();
         } catch (SQLException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         connection = null;
     }
     
     /**
      * Shorthand for Connection.createStatement with the added benefit of
      * checking for a proper connection.
      * @return  A new Statement object.
      */
     public Statement createStatement()
     {
         try
         {
             connect();
             return connection.createStatement();
         }
         catch (SQLException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
             return null;
         }
     }
     
     /**
      * Performs a query on the database (if connected) and returns the raw
      * result in a read-only, forward-only ResultSet.
      * @param sql   The SQL statement to execute. Is not checked for errors
      *              prior to execution.
      * @return  The ResultSet from the query or null if the statement did not
      *          generate a ResultSet (as is the case with INSERT/UPDATE/etc).
      */
     public ResultSet queryAsResultSet(String sql) throws SQLException
     {
         Statement stat;
 
         // stat = connection.createStatement();
         stat = createStatement();
         
         if (stat.execute(sql))
         {
             return stat.getResultSet();
         }
         else return null;
     }
     
     @Override
     protected void finalize() throws Throwable
     {
         // TODO Auto-generated method stub
         super.finalize();
         disconnect();
     }
 }
