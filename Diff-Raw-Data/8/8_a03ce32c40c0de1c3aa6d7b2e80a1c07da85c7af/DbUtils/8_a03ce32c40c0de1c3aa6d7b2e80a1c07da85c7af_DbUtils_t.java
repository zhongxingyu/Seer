 package com.dooioo.upload.utils;
 
import com.dooioo.commons.json.JsonUtils;
 import com.dooioo.upload.image.ImageArgConvert;
 import org.apache.commons.dbcp.BasicDataSource;
 
 import javax.sql.DataSource;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA at 13-9-9 下午5:35.
  *
  * @author 焦义贵
  * @since 1.0
  *        To change this template use File | Settings | File Templates.
  */
 public class DbUtils {
     private static DataSource dataSource;
     private static DbUtils dbUtils;
 
     private String url;
     private String driver;
     private String user;
     private String password;
 
     private DbUtils() {
         Properties properties = new Properties();
         try {
             properties.load(getClass().getClassLoader().getResourceAsStream("global.properties"));
             properties.load(getClass().getClassLoader().getResourceAsStream("jdbc.properties"));
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         String env = properties.getProperty("env","test");
         if("production".equals(env)){    //默认都是test
             env = "test";
         }
         url = properties.getProperty(env + ".fileupload.jdbc.url","jdbc:sqlserver://10.8.1.142:1433;DatabaseName=activeMQ");
         driver = properties.getProperty(env + ".fileupload.jdbc.driver","com.microsoft.sqlserver.jdbc.SQLServerDriver");
         user = properties.getProperty(env + ".fileupload.jdbc.user","dooiooadmin");
         password = properties.getProperty(env + ".fileupload.jdbc.password","5a2eff87efec511e");
 
         dataSource = initDataSource();
     }
 
     public synchronized static DbUtils getInstance() {
         if(dbUtils == null) {
             dbUtils = new DbUtils();
         }
         return dbUtils;
     }
 
    public boolean insertTask(String path,List<ImageArgConvert> imageArgConverts) throws IOException {
        String sql = String.format("insert into asyncImageTask(path,imageArgConvertJson) values ('%s','%s')", path, JsonUtils.objectToJson(imageArgConverts).replaceAll("'", "''"));
         Connection connection= null;
         Statement s = null;
         try {
             connection = dataSource.getConnection();
             s = connection.createStatement();
             return s.executeUpdate(sql) > 0;
         } catch (SQLException e) {
 
         } finally {
             if(connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                 }
             }
             if(s != null) {
                 try {
                     s.close();
                 } catch (SQLException e) {
 
                 }
             }
         }
         return false;
     }
 
     private BasicDataSource initDataSource() {
         BasicDataSource p = new BasicDataSource();
         p.setUrl(url);
         p.setDriverClassName(driver);
         p.setUsername(user);
         p.setPassword(password);
         p.setTestWhileIdle(false);
         p.setValidationQuery("SELECT 1");
         p.setTestOnReturn(false);
         p.setTimeBetweenEvictionRunsMillis(30000);
         p.setMaxActive(50);
         p.setInitialSize(10);
         p.setMaxWait(300000);
         p.setMinEvictableIdleTimeMillis(30000);
         p.setMinIdle(10);
         p.setLogAbandoned(true);
         p.setRemoveAbandoned(true);
         p.setRemoveAbandonedTimeout(3600);
         p.setTestOnBorrow(true);
         return p;
     }
 
 }
