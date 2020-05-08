 package net.sourceforge.guacamole.net.cvp;
 
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import net.sourceforge.guacamole.protocol.GuacamoleConfiguration;
 
 public class MySQLConnector extends AbstractConnector implements Connector {
 
     protected java.sql.Connection connection;
     protected PreparedStatement statement;
     protected PreparedStatement deleteStatement;
     protected String dbUrl = "jdbc:mysql://localhost:3306/cvp";
     protected String user = null;
     protected String password = null;
 
     public void init() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
             Properties properties = new Properties();
             properties.put("user", user);
             properties.put("password", password);
             connection = DriverManager.getConnection(dbUrl, properties);
             statement = connection.prepareStatement("select config from configuration where id = ?");
             deleteStatement = connection.prepareStatement("delete from configuration where id = ?");
         } catch (ClassNotFoundException cnfe) {
             throw new RuntimeException("JDBC driver not found");
         } catch (SQLException sqle) {
            sqle.printStackTrace();
             throw new RuntimeException("No database connection: " + dbUrl + " " + user + "/" + password);
         }
 
     }
 
     public void destroy() {
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException sqle) {
                 throw new RuntimeException("Close connection exception: " + dbUrl + " " + user + "/" + password);
             }
         }
     }
 
     @Override
     public Map<String, GuacamoleConfiguration> findConfigurations(String key) {
         try {
             statement.setString(1, key);
             ResultSet rs = statement.executeQuery();
             String value = null;
             while (rs.next()) {
                 value = rs.getString(1);
             }
             if (value != null) {
                 
                 deleteStatement.setString(1, key);
                 deleteStatement.executeUpdate();
                connection.commit();
                 logger.debug(key + " -> " + value);
                                 
                 AuthResponse authResponse = convert(value);
                 if (authResponse != null) {
                     Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();
                     for (Connection con : authResponse.getConnections()) {
                         GuacamoleConfiguration config = new GuacamoleConfiguration();
                         config.setProtocol(con.getProtocol());
                         config.setParameter(PROP_KEY_HOSTNAME, con.getHost());
                         config.setParameter(PROP_KEY_PORT, String.valueOf(con.getPort()));
                         config.setParameter(PROP_KEY_PASSWORD, con.getPassword());
 
                         configs.put(con.getName(), config);
 
                         logger.info("Name:" + con.getName());
                         logger.info("Host:" + con.getHost());
                         logger.info("Protocol:" + con.getProtocol());
                         logger.info("Port:" + con.getPort());
                         //logger.info("Password:" + con.getPassword());
                     }
                     return configs;
                 }
             }
         } catch (SQLException sqle) {
             sqle.printStackTrace();
         }
         return null;
     }
 
     public void setDbUrl(String dbUrl) {
         this.dbUrl = dbUrl;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
 }
