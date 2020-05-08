 package jKeeper;
 
 import jKeeper.bean.BeanParser;
 import jKeeper.bean.BeanProp;
 import jKeeper.db.DbParser;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.sql.DataSource;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class Keeper {
     private static final Logger logger = LoggerFactory.getLogger(Keeper.class);
     private final DataSource dataSource;
 
     public Keeper(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
 
     public Connection getConnection() throws SQLException {
         return dataSource.getConnection();
     }
 
     public static void close(Connection connection) {
         try {
             if (connection != null)
                 connection.close();
         } catch (SQLException sqle) {
             logger.error(sqle.getMessage(), sqle);
         }
     }
 
     public static void close(Statement statement) {
         try {
             if (statement != null)
                 statement.close();
         } catch (SQLException sqle) {
             logger.error(sqle.getMessage(), sqle);
         }
     }
 
     public static void close(PreparedStatement statement) {
         try {
             if (statement != null)
                 statement.close();
         } catch (SQLException sqle) {
             logger.error(sqle.getMessage(), sqle);
         }
     }
 
     public static void close(ResultSet rst) {
         try {
             if (rst != null)
                 rst.close();
         } catch (SQLException sqle) {
             logger.error(sqle.getMessage(), sqle);
         }
     }
 
 
     public static void close(Connection conn, ResultSet rst) {
         close(conn);
         close(rst);
     }
 
     public static void close(Connection conn, PreparedStatement pst, ResultSet rst) {
         close(conn);
         close(pst);
         close(rst);
     }
 
     public static void close(Connection conn, Statement st) {
         close(conn);
         close(st);
     }
 
     public static void close(Connection conn, Statement pst, ResultSet rst) {
         close(conn);
         close(pst);
         close(rst);
     }
 
     public static void close(Connection conn, PreparedStatement pst) {
         close(conn);
         close(pst);
     }
 
     /**
      * Retrieve single object
      *
      * @param sql
      * @param type
      * @return
      * @throws SQLException
      */
     public <T> T one(String sql, Class<T> type) throws SQLException {
         return one(sql, type, null);
     }
 
     /**
      * Retrieve single object
      *
      * @param sql
      * @param type
      * @param columnMapper
      * @return
      * @throws SQLException
      */
     public <T> T one(String sql, Class<T> type, HashMap<String, String> columnMapper) throws SQLException {
         Connection connection = this.getConnection();
         Statement st = connection.createStatement();
         ResultSet rs = st.executeQuery(sql);
         T bean = null;
         try {
            rs.next();
            bean = this.createBean(type, rs, DbParser.getColumns(rs.getMetaData(), getBeanParser(type).getProps(columnMapper)));
         } catch (SQLException e) {
             logger.error(e.getMessage());
             throw new SQLException(e.getMessage());
         } finally {
             Keeper.close(connection, st, rs);
         }
         return bean;
     }
 
     /**
      * Retrieve  list of objects
      *
      * @param sql
      * @param type
      * @param <T>
      * @return
      * @throws SQLException
      */
     public <T> List<T> list(String sql, Class<T> type) throws SQLException {
         return list(sql, type, null);
     }
 
     /**
      * Retrieve  list of objects
      *
      * @param sql
      * @param type
      * @param columnMapper
      * @return
      * @throws SQLException
      */
     public <T> List<T> list(String sql, Class<T> type, HashMap<String, String> columnMapper) throws SQLException {
         ArrayList<T> list = new ArrayList<T>();
         Connection connection = this.getConnection();
         Statement st = connection.createStatement();
         ResultSet rs = st.executeQuery(sql);
         try {
             HashMap<String, BeanProp> cols = DbParser.getColumns(rs.getMetaData(), getBeanParser(type).getProps(columnMapper));
             while (rs.next()) {
                 list.add(this.createBean(type, rs, cols));
             }
         } catch (SQLException e) {
             logger.error(e.getMessage());
             throw new SQLException(e.getMessage());
         } finally {
             Keeper.close(connection, st, rs);
         }
         return list;
     }
 
     /**
      * Insert object to db
      *
      * @param obj
      * @return
      * @throws SQLException
      */
     public boolean insert(Object obj) throws SQLException {
         Class type = obj.getClass();
         HashMap<String, BeanProp> props = getBeanParser(type).getProps();
         List<String> columns = new ArrayList<String>();
         String values = "";
         boolean firstVal = true;
         for (BeanProp prop : props.values()) {
             if (!prop.isSkipped() && !prop.isId()) {
                 columns.add(prop.getColumnName());
                 try {
                     if (!firstVal) {
                         values += ",";
                     }
                     firstVal = false;
 
                     Method getter = type.getDeclaredMethod(prop.getGetter());
                     values += getValue(prop, getter.invoke(obj));
                 } catch (NoSuchMethodException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Getter not found " + prop.getGetter() + ": " + e.getLocalizedMessage());
                 } catch (InvocationTargetException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Cant invoke getter" + prop.getGetter() + ": " + e.getLocalizedMessage());
                 } catch (IllegalAccessException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Cant access getter" + prop.getGetter() + ": " + e.getLocalizedMessage());
                 }
             }
         }
         String sql = "INSERT INTO " + getBeanParser(type).getTable() + " ([" + StringUtils.join(columns, "],[") + "]) values (" + values + ")";
         return execute(sql);
     }
 
     /**
      * Update object in db
      *
      * @param obj
      * @return
      * @throws SQLException
      */
     public boolean update(Object obj) throws SQLException {
         Class type = obj.getClass();
         HashMap<String, BeanProp> props = getBeanParser(type).getProps();
         List<String> updates = new ArrayList<String>();
         String idCol = "";
         String idVal = "";
         for (BeanProp prop : props.values()) {
             if (!prop.isSkipped()) {
                 try {
                     Method getter = type.getDeclaredMethod(prop.getGetter());
                     String value = getValue(prop, getter.invoke(obj));
                     if (prop.isId()) {
                         idCol = prop.getColumnName();
                         idVal = value;
                     } else {
                         updates.add(prop.getColumnName() + "=" + value);
                     }
                 } catch (NoSuchMethodException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Getter not found " + prop.getGetter() + ": " + e.getLocalizedMessage());
                 } catch (InvocationTargetException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Cant invoke getter" + prop.getGetter() + ": " + e.getLocalizedMessage());
                 } catch (IllegalAccessException e) {
                     logger.error(e.getMessage());
                     throw new SQLException(
                             "Cant access getter" + prop.getGetter() + ": " + e.getLocalizedMessage());
                 }
             }
         }
         String sql = "UPDATE " + getBeanParser(type).getTable() + " SET " + StringUtils.join(updates, ",") + " WHERE " + idCol + "=" + idVal;
         return execute(sql);
     }
 
     /**
      * Execute free sql query
      *
      * @param sql
      * @return
      * @throws SQLException
      */
     public boolean execute(String sql) throws SQLException {
         Connection connection = this.getConnection();
         Statement st = connection.createStatement();
         try {
             return st.execute(sql);
         } catch (SQLException e) {
             logger.error(e.getMessage());
             throw new SQLException(e.getMessage());
         } finally {
             close(connection, st);
         }
     }
 
     private String getValue(BeanProp prop, Object val) {
         boolean withBrace;
 
         switch (prop.getColumnType()) {
             case INT:
                 withBrace = false;
             case VARCHAR:
             case DATE:
             case DATETIME:
                 withBrace = true;
             default:
                 withBrace = true;
         }
 
         String sqlVal;
         if (val == null) {
             withBrace = false;
             sqlVal = "NULL";
         } else {
             sqlVal = val.toString();
         }
 
         return (withBrace ? "'" : "") + sqlVal + (withBrace ? "'" : "");
     }
 
     private BeanParser getBeanParser(Class type) {
         return new BeanParser(type);
     }
 
     private <T> T createBean(Class<T> type, ResultSet rs, HashMap<String, BeanProp> columns) throws SQLException {
         T bean = this.newInstance(type);
         String getterName = "";
         for (BeanProp column : columns.values()) {
             try {
 
                 getterName = "get" + StringUtils.capitalize(column.getType().getSimpleName());
                 Method getter = ResultSet.class.getMethod(getterName, String.class);
 
                 Method setter = type.getDeclaredMethod(column.getSetter(), column.getType());
                 setter.invoke(bean, getter.invoke(rs, column.getColumnName()));
 
 //by field
 //                Field field = type.getDeclaredField(column.getFieldName());
 //                field.setAccessible(true);
 //                field.set(bean, getter.invoke(rs, column.getColumnName()));
 
             } catch (InvocationTargetException e) {
                 throw new SQLException(
                         "Wrong target for " + getterName + " with " + column.getColumnName() + " : " + e.getLocalizedMessage());
             } catch (NoSuchMethodException e) {
                 throw new SQLException(
                         "Method not found " + getterName + ": " + e.getLocalizedMessage());
 //            } catch (NoSuchFieldException e) {
 //                throw new SQLException(
 //                        "Field not found " + column.getFieldName() + ": " + e.getLocalizedMessage());
             } catch (IllegalAccessException e) {
                 throw new SQLException(
                         "Cant access rs getter " + getterName + ": " + e.getLocalizedMessage());
             }
         }
         return bean;
     }
 
     private <T> T newInstance(Class<T> c) throws SQLException {
         try {
             return c.newInstance();
 
         } catch (InstantiationException e) {
             throw new SQLException(
                     "Cannot create " + c.getName() + ": " + e.getMessage());
 
         } catch (IllegalAccessException e) {
             throw new SQLException(
                     "Cannot create " + c.getName() + ": " + e.getMessage());
         }
     }
 
 }
