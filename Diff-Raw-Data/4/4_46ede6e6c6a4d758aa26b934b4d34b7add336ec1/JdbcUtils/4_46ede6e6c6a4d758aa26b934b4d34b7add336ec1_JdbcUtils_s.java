 package ddth.dasp.framework.dbc;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JdbcUtils {
     private static final Pattern PATTERN_PARAMS_PLACEHOLDER = Pattern.compile("\\@\\{([^\\}]+)\\}");
 
     private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUtils.class);
 
     /**
      * Quietly closes JDBC resources.
      * 
      * @param conn
      * @param stm
      * @param rs
      */
     public static void closeResources(Connection conn, Statement stm, ResultSet rs) {
         try {
             if (rs != null) {
                 rs.close();
             }
         } catch (Exception e) {
             LOGGER.warn(e.getMessage(), e);
         }
         try {
             if (stm != null) {
                 stm.close();
             }
         } catch (Exception e) {
             LOGGER.warn(e.getMessage(), e);
         }
         try {
             if (conn != null) {
                 conn.close();
             }
         } catch (Exception e) {
             LOGGER.warn(e.getMessage(), e);
         }
     }
 
     /**
      * Prepares a SQL statement.
      * 
      * @param conn
      * @param sql
      * @param params
      * @return
      * @throws SQLException
      */
     public static PreparedStatement prepareStatement(Connection conn, String sql, Object[] params)
             throws SQLException {
         return prepareStatement(conn, sql, params, false);
     }
 
     /**
      * Prepares a SQL statement.
      * 
      * @param conn
      * @param sql
      * @param params
      * @param isCallable
      *            specify if a {@link CallableStatement} should be returned
      * @return
      * @throws SQLException
      */
     public static PreparedStatement prepareStatement(Connection conn, String sql, Object[] params,
             boolean isCallable) throws SQLException {
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Preparing statement [" + sql + "] with arguments: " + params);
         }
         PreparedStatement stmt = isCallable ? conn.prepareCall(sql) : conn.prepareStatement(sql);
         if (params != null && params.length > 0) {
             int index = 1;
             for (Object param : params) {
                if (param instanceof String) {
                    stmt.setString(index, (String) param);
                 } else if (param instanceof Integer) {
                     stmt.setInt(index, (Integer) param);
                 } else if (param instanceof Long) {
                     stmt.setLong(index, (Long) param);
                 } else if (param instanceof Float) {
                     stmt.setFloat(index, (Float) param);
                 } else if (param instanceof Double) {
                     stmt.setDouble(index, (Double) param);
                 } else {
                     stmt.setObject(index, param);
                 }
                 index++;
             }
         }
         return stmt;
     }
 
     /**
      * Prepares a SQL statement.
      * 
      * @param conn
      * @param sql
      * @param params
      * @return
      * @throws SQLException
      */
     public static PreparedStatement prepareStatement(Connection conn, String sql,
             Map<String, Object> params) throws SQLException {
         return prepareStatement(conn, sql, params, false);
     }
 
     /**
      * Prepares a SQL statement by name.
      * 
      * @param conn
      * @param sql
      * @param params
      * @param isCallable
      *            specify if a {@link CallableStatement} should be returned
      * @return the {@link PreparedStatement} ready for execution
      * @throws SQLException
      */
     public static PreparedStatement prepareStatement(Connection conn, String sql,
             Map<String, Object> params, boolean isCallable) throws SQLException {
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Preparing statement [" + sql + "] with arguments: " + params);
         }
         String[] paramsByIndex = extractParams(sql);
         List<Object> paramsValueByIndex = new LinkedList<Object>();
         for (String paramName : paramsByIndex) {
             if (params != null && params.containsKey(paramName)) {
                 paramsValueByIndex.add(params.get(paramName));
             } else {
                 throw new SQLException("Missing value for parameter " + paramName);
             }
         }
         String cleanSql = PATTERN_PARAMS_PLACEHOLDER.matcher(sql).replaceAll("?");
         return prepareStatement(conn, cleanSql, paramsValueByIndex.toArray(), isCallable);
     }
 
     /**
      * Extracts parameters from a SQL statement and preserves theirs index.
      * 
      * For example: with the SQL statement
      * <code>INSERT INTO user (id, email, password) VALUES (${id}, ${email}, ${password})</code>
      * , the method will return <code>["id", "email", "password"]</code>
      * 
      * @param sql
      * @return
      */
     public static String[] extractParams(String sql) {
         List<String> result = new LinkedList<String>();
         Matcher matcher = PATTERN_PARAMS_PLACEHOLDER.matcher(sql);
         while (matcher.find()) {
             result.add(matcher.group(1));
         }
         return result.toArray(new String[0]);
     }
 }
