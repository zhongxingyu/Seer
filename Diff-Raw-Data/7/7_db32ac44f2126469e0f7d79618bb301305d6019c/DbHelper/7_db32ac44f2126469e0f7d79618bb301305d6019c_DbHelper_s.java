 package hu.sch.kurzuscsere.logic.db;
 
import hu.sch.kurzuscsere.WicketApplication;
 import hu.sch.kurzuscsere.config.ConfigKeys;
 import java.sql.Connection;
 import java.sql.SQLException;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author balo
  */
 public abstract class DbHelper {
 
    private static final Logger log = LoggerFactory.getLogger(WicketApplication.class);
     private static final String EXCEPTION_MSG = "Can't get database connection.";
 
     private DbHelper() {
     }
 
    public static final Connection getConnection() {
         Connection connection = null;
         try {
             InitialContext context = new InitialContext();
             DataSource dataSource = (DataSource) context.lookup(ConfigKeys.JDBC_RES);
             connection = dataSource.getConnection();
             if (connection == null) {
                 throw new Exception("connection is null");
             }
         } catch (NamingException e) {
             log.error(EXCEPTION_MSG, e);
         } catch (SQLException e) {
             log.error(EXCEPTION_MSG, e);
         } catch (Exception e) {
             log.error(EXCEPTION_MSG, e);
         }
         return connection;
     }
 }
